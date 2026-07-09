<template>
  <div class="app-container">
    <!-- 搜索表单 -->
    <div class="search-wrapper">
      <el-form :model="queryParams" ref="queryFormRef" :inline="true">
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="queryParams.username"
            placeholder="请输入用户名"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="操作内容" prop="operation">
          <el-input
            v-model="queryParams.operation"
            placeholder="请输入操作内容"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="耗时等级" prop="durationLevel">
          <el-select v-model="queryParams.durationLevel" placeholder="请选择" clearable>
            <el-option label="快速 (<=200ms)" value="fast" />
            <el-option label="正常 (200-1000ms)" value="normal" />
            <el-option label="慢速 (>1000ms)" value="slow" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作时间">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">
            <el-icon><Search /></el-icon>搜索
          </el-button>
          <el-button @click="resetQuery">
            <el-icon><Refresh /></el-icon>重置
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-card class="box-card">
      <!-- 操作按钮区域 -->
      <template #header>
        <div class="card-header">
          <ButtonGroup>
            <el-button
              v-permission="['sys:operateLog:delete']"
              type="danger"
              icon="Delete"
              :disabled="selectedIds.length === 0"
              @click="handleBatchDelete"
            >批量删除</el-button>
          </ButtonGroup>
        </div>
      </template>

      <!-- 表格区域 -->
      <el-table
        v-loading="loading"
        :data="logList"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="expand">
          <template #default="scope">
            <el-scrollbar max-height="400px">
              <div class="expand-container">
                <!-- 基本信息 -->
                <el-descriptions :column="2" border size="small">
                  <el-descriptions-item label="请求接口" :span="2">
                    {{ scope.row.classPath }}.{{ scope.row.methodName }}()
                  </el-descriptions-item>
                  <el-descriptions-item label="请求URL" :span="2">
                    <el-tag size="small" :type="getMethodType(scope.row.requestMethod)">
                      {{ scope.row.requestMethod }}
                    </el-tag>
                    {{ scope.row.requestUrl }}
                  </el-descriptions-item>
                  <el-descriptions-item label="请求参数" :span="2">
                    <el-input
                      type="textarea"
                      :model-value="formatJson(scope.row.requestParams)"
                      :rows="3"
                      readonly
                    />
                  </el-descriptions-item>
                  <el-descriptions-item label="响应体" :span="2">
                    <el-input
                      type="textarea"
                      :model-value="formatJson(scope.row.responseBody)"
                      :rows="5"
                      readonly
                    />
                  </el-descriptions-item>
                  <el-descriptions-item label="User-Agent">
                    {{ scope.row.userAgent || '-' }}
                  </el-descriptions-item>
                  <el-descriptions-item label="设备信息">
                    {{ scope.row.os || '-' }} / {{ scope.row.browser || '-' }}
                  </el-descriptions-item>
                  <el-descriptions-item label="错误信息" v-if="scope.row.errorMsg" :span="2">
                    <el-tag type="danger">{{ scope.row.errorMsg }}</el-tag>
                  </el-descriptions-item>
                </el-descriptions>
              </div>
            </el-scrollbar>
          </template>
        </el-table-column>
        <el-table-column align="center" type="selection" width="55" />
        <el-table-column
          prop="username"
          align="center"
          width="100"
          label="操作人"
        />
        <el-table-column
          prop="operationName"
          align="center"
          width="150"
          label="操作名称"
          show-overflow-tooltip
        />
        <el-table-column
          prop="module"
          align="center"
          width="120"
          label="模块"
          show-overflow-tooltip
        />
        <el-table-column
          prop="requestUrl"
          align="center"
          label="请求接口"
          show-overflow-tooltip
        />
        <el-table-column
          align="center"
          width="100"
          label="请求方式"
        >
          <template #default="scope">
            <el-tag :type="getMethodType(scope.row.requestMethod)" size="small">
              {{ scope.row.requestMethod }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          align="center"
          width="100"
          label="状态码"
        >
          <template #default="scope">
            <el-tag :type="scope.row.responseCode === 200 ? 'success' : 'danger'" size="small">
              {{ scope.row.responseCode || '-' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ip" width="130" align="center" label="IP" />
        <el-table-column
          align="center"
          width="100"
          label="操作系统"
        >
          <template #default="scope">
            <span>{{ scope.row.os || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column
          align="center"
          width="100"
          label="浏览器"
        >
          <template #default="scope">
            <span>{{ scope.row.browser || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column align="center" width="120" label="请求耗时">
          <template #default="scope">
            <el-tag :type="getDurationType(scope.row.durationLevel)" size="small">
              {{ scope.row.spendTime }}ms
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column align="center" width="80" label="耗时等级">
          <template #default="scope">
            <el-tag :type="getDurationType(scope.row.durationLevel)" size="small" effect="dark">
              {{ getDurationLabel(scope.row.durationLevel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="createTime"
          align="center"
          width="180"
          label="创建时间"
        />
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="danger" link @click="handleDelete(row)" v-permission="['sys:operateLog:delete']">
              <el-icon><Delete /></el-icon>删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页区域 -->
      <div class="pagination-container">
        <el-pagination
          background
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 30, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getOperationLogsApi,
  deleteOperationLogsApi,
} from '@/api/system/operLog'

const loading = ref(false)
const total = ref(0)
const logList = ref<any[]>([])
const selectedIds = ref<number[]>([])
const dateRange = ref<[string, string]>()

// 查询参数
const queryParams = reactive<any>({
  pageNum: 1,
  pageSize: 10,
  username: '',
  operation: '',
  durationLevel: ''
})

// 获取请求方式对应的样式
const getMethodType = (method: string) => {
  const map: Record<string, string> = {
    GET: 'primary',
    POST: 'success',
    PUT: 'warning',
    DELETE: 'danger'
  }
  return map[method] || 'info'
}

// 获取耗时等级对应的样式
const getDurationType = (level: string) => {
  const map: Record<string, string> = {
    fast: 'success',
    normal: 'warning',
    slow: 'danger'
  }
  return map[level] || 'info'
}

// 获取耗时等级标签
const getDurationLabel = (level: string) => {
  const map: Record<string, string> = {
    fast: '快速',
    normal: '正常',
    slow: '慢速'
  }
  return map[level] || '-'
}

// 格式化 JSON
const formatJson = (json: string) => {
  if (!json) return '-'
  try {
    return JSON.stringify(JSON.parse(json), null, 2)
  } catch {
    return json
  }
}

// 监听日期范围变化
watch(dateRange, (val) => {
  if (val) {
    queryParams.startTime = val[0]
    queryParams.endTime = val[1]
  } else {
    queryParams.startTime = undefined
    queryParams.endTime = undefined
  }
})

// 获取日志列表
const getList = async () => {
  loading.value = true
  try {
    const { data } = await getOperationLogsApi(queryParams)
    logList.value = data.records
    total.value = data.total
  } catch (error) {
    console.error('获取操作日志失败:', error)
  }
  loading.value = false
}

// 搜索
const handleQuery = () => {
  queryParams.pageNum = 1
  getList()
}

// 重置
const resetQuery = () => {
  dateRange.value = undefined
  queryParams.pageNum = 1
  queryParams.pageSize = 10
  queryParams.username = ''
  queryParams.operation = ''
  queryParams.durationLevel = ''
  queryParams.startTime = undefined
  queryParams.endTime = undefined
  getList()
}

// 选择变化
const handleSelectionChange = (selection: any[]) => {
  selectedIds.value = selection.map(item => item.id)
}

// 批量删除
const handleBatchDelete = () => {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请选择要删除的记录')
    return
  }

  ElMessageBox.confirm(`是否确认删除 ${selectedIds.value.length} 个操作日志?`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteOperationLogsApi(selectedIds.value)
      ElMessage.success('删除成功')
      getList()
    } catch (error) {
    }
  }).catch(() => {
  })
}

// 删除
const handleDelete = (row: any) => {
  ElMessageBox.confirm(`确定要删除 ${row.username} 这个用户的操作日志吗？`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteOperationLogsApi(row.id)
      ElMessage.success('删除成功')
      getList()
    } catch (error) {
    }
  }).catch(() => {
  })
}

// 分页大小变化
const handleSizeChange = (val: number) => {
  queryParams.pageSize = val
  getList()
}

// 页码变化
const handleCurrentChange = (val: number) => {
  queryParams.pageNum = val
  getList()
}

// 初始化
getList()
</script>

<style scoped>
.mb-2 {
  margin-bottom: 16px;
}

.expand-container {
  padding: 20px;
}

.expand-container :deep(.el-descriptions) {
  margin-bottom: 0;
}

.expand-container :deep(.el-descriptions__label) {
  width: 100px;
}

.expand-container :deep(.el-textarea__inner) {
  font-family: 'Courier New', Courier, monospace;
  font-size: 12px;
}
</style>
