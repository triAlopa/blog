<template>
  <div class="app-container">
    <!-- 搜索表单 -->
    <div class="search-wrapper">
      <el-form ref="queryFormRef" :model="queryParams" :inline="true">
        <el-form-item label="模板名称" prop="templateName">
          <el-input v-model="queryParams.templateName" placeholder="请输入模板名称" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="模板编码" prop="templateCode">
          <el-input v-model="queryParams.templateCode" placeholder="请输入模板编码" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 操作按钮区域 -->
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <ButtonGroup>
            <el-button type="primary" icon="Plus" @click="handleAdd" v-permission="['tool:emailTemplate:add']">新增</el-button>
          </ButtonGroup>
        </div>
      </template>

      <!-- 数据表格 -->
      <el-table v-loading="loading" :data="templateList" style="width: 100%">
        <el-table-column label="模板编码" align="center" prop="templateCode" width="150" show-overflow-tooltip />
        <el-table-column label="模板名称" align="center" prop="templateName" width="150" show-overflow-tooltip />
        <el-table-column label="邮件主题" align="center" prop="subject" show-overflow-tooltip />
        <el-table-column label="状态" align="center" width="80">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" align="center" prop="createTime" width="160" />
        <el-table-column label="操作" align="center" width="200" fixed="right">
          <template #default="scope">
            <el-button type="primary" link icon="View" @click="handleView(scope.row)">查看</el-button>
            <el-button type="primary" link icon="Edit" @click="handleEdit(scope.row)" v-permission="['tool:emailTemplate:edit']">编辑</el-button>
            <el-button type="danger" link icon="Delete" @click="handleDelete(scope.row)" v-permission="['tool:emailTemplate:delete']">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页组件 -->
      <div class="pagination-container">
        <el-pagination v-model:current-page="queryParams.pageNum" v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 30, 50]" :total="total" :background="true"
          layout="total, sizes, prev, pager, next, jumper" @size-change="handleSizeChange"
          @current-change="handleCurrentChange" />
      </div>
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="800px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="模板编码" prop="templateCode">
              <el-input v-model="form.templateCode" placeholder="请输入模板编码" :disabled="!!form.id" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="模板名称" prop="templateName">
              <el-input v-model="form.templateName" placeholder="请输入模板名称" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="邮件主题" prop="subject">
              <el-input v-model="form.subject" placeholder="请输入邮件主题" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio :label="1">启用</el-radio>
                <el-radio :label="0">禁用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="模板说明" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入模板说明" />
        </el-form-item>
        <el-form-item label="模板内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="15" placeholder="请输入HTML模板内容" />
        </el-form-item>
        <el-form-item label="变量说明">
          <el-tag v-for="item in variables" :key="item.key" class="variable-tag" @click="insertVariable(item.key)">
            {{ item.key }} - {{ item.desc }}
          </el-tag>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="handleSubmit">确 定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 预览对话框 -->
    <el-dialog title="模板预览" v-model="previewVisible" width="800px" append-to-body>
      <div class="preview-header">
        <p><strong>模板编码：</strong>{{ previewData.templateCode }}</p>
        <p><strong>模板名称：</strong>{{ previewData.templateName }}</p>
        <p><strong>邮件主题：</strong>{{ previewData.subject }}</p>
      </div>
      <el-divider />
      <div class="preview-content" v-html="previewData.content"></div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import request from '@/utils/request'

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  templateName: undefined,
  templateCode: undefined
})

const loading = ref(false)
const templateList = ref([])
const total = ref(0)
const queryFormRef = ref<FormInstance>()

// 对话框相关
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref<FormInstance>()
const form = reactive({
  id: undefined,
  templateCode: '',
  templateName: '',
  subject: '',
  content: '',
  remark: '',
  status: 1
})

const rules: FormRules = {
  templateCode: [{ required: true, message: '请输入模板编码', trigger: 'blur' }],
  templateName: [{ required: true, message: '请输入模板名称', trigger: 'blur' }],
  subject: [{ required: true, message: '请输入邮件主题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入模板内容', trigger: 'blur' }]
}

// 预览相关
const previewVisible = ref(false)
const previewData = reactive({
  templateCode: '',
  templateName: '',
  subject: '',
  content: ''
})

// 变量列表
const variables = [
  { key: '${code}', desc: '验证码' },
  { key: '${siteName}', desc: '网站名称' },
  { key: '${siteUrl}', desc: '网站地址' },
  { key: '${logoUrl}', desc: 'Logo地址' },
  { key: '${username}', desc: '用户名' },
  { key: '${email}', desc: '邮箱地址' }
]

// 获取列表
const getList = async () => {
  loading.value = true
  try {
    const res = await request({
      url: '/tool/email-template/list',
      method: 'get',
      params: queryParams
    })
    templateList.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error('获取邮件模板列表失败', error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleQuery = () => {
  queryParams.pageNum = 1
  getList()
}

// 重置
const resetQuery = () => {
  queryFormRef.value?.resetFields()
  handleQuery()
}

// 分页
const handleSizeChange = (val: number) => {
  queryParams.pageSize = val
  getList()
}

const handleCurrentChange = (val: number) => {
  queryParams.pageNum = val
  getList()
}

// 新增
const handleAdd = () => {
  resetForm()
  dialogTitle.value = '添加邮件模板'
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row: any) => {
  resetForm()
  Object.assign(form, row)
  dialogTitle.value = '编辑邮件模板'
  dialogVisible.value = true
}

// 查看
const handleView = (row: any) => {
  Object.assign(previewData, row)
  previewVisible.value = true
}

// 删除
const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm('确认要删除该邮件模板吗？', '提示', {
      type: 'warning'
    })
    await request({
      url: `/tool/email-template/${row.id}`,
      method: 'delete'
    })
    ElMessage.success('删除成功')
    getList()
  } catch (error) {
    console.error('删除失败', error)
  }
}

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
    if (form.id) {
      await request({
        url: '/tool/email-template',
        method: 'put',
        data: form
      })
      ElMessage.success('修改成功')
    } else {
      await request({
        url: '/tool/email-template',
        method: 'post',
        data: form
      })
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    getList()
  } catch (error) {
    console.error('提交失败', error)
  }
}

// 重置表单
const resetForm = () => {
  form.id = undefined
  form.templateCode = ''
  form.templateName = ''
  form.subject = ''
  form.content = ''
  form.remark = ''
  form.status = 1
  formRef.value?.resetFields()
}

// 插入变量
const insertVariable = (key: string) => {
  const textarea = document.querySelector('.el-textarea__inner') as HTMLTextAreaElement
  if (textarea) {
    const start = textarea.selectionStart
    const end = textarea.selectionEnd
    const content = form.content
    form.content = content.substring(0, start) + key + content.substring(end)
    // 设置光标位置
    setTimeout(() => {
      textarea.focus()
      textarea.setSelectionRange(start + key.length, start + key.length)
    }, 0)
  }
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.search-wrapper {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.variable-tag {
  margin-right: 8px;
  margin-bottom: 8px;
  cursor: pointer;
}

.variable-tag:hover {
  color: var(--el-color-primary);
  border-color: var(--el-color-primary);
}

.preview-header p {
  margin: 8px 0;
}

.preview-content {
  border: 1px solid var(--el-border-color);
  padding: 16px;
  border-radius: 4px;
  max-height: 500px;
  overflow-y: auto;
}
</style>
