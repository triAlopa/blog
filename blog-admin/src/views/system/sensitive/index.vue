<template>
  <div class="sensitive-container">
    <el-row :gutter="20">
      <!-- 左侧：敏感词管理 -->
      <el-col :span="14">
        <el-card class="box-card">
          <template #header>
            <div class="card-header">
              <span>敏感词管理</span>
              <div>
                <el-button
                  type="primary"
                  icon="Plus"
                  @click="handleAddWord"
                >新增</el-button>
                <el-button
                  type="danger"
                  icon="Delete"
                  :disabled="selectedWordIds.length === 0"
                  @click="handleBatchDeleteWord"
                >批量删除</el-button>
                <el-button
                  type="success"
                  icon="Upload"
                  @click="handleBatchAddWord"
                >批量导入</el-button>
                <el-button
                  type="warning"
                  icon="Refresh"
                  @click="handleReload"
                >刷新词库</el-button>
              </div>
            </div>
          </template>

          <!-- 搜索区域 -->
          <el-form :inline="true" class="search-form">
            <el-form-item label="敏感词">
              <el-input
                v-model="wordQuery.word"
                placeholder="请输入敏感词"
                clearable
                @keyup.enter="handleWordQuery"
              />
            </el-form-item>
            <el-form-item label="类型">
              <el-select v-model="wordQuery.wordType" placeholder="请选择类型" clearable>
                <el-option label="自定义" value="CUSTOM" />
                <el-option label="官方" value="OFFICIAL" />
              </el-select>
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="wordQuery.status" placeholder="请选择状态" clearable>
                <el-option label="启用" value="ACTIVE" />
                <el-option label="禁用" value="DISABLED" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleWordQuery">
                <el-icon><Search /></el-icon>搜索
              </el-button>
              <el-button @click="resetWordQuery">
                <el-icon><Refresh /></el-icon>重置
              </el-button>
            </el-form-item>
          </el-form>

          <!-- 表格区域 -->
          <el-table
            v-loading="wordLoading"
            :data="wordList"
            @selection-change="handleWordSelectionChange"
            style="width: 100%"
            max-height="500"
          >
            <el-table-column type="selection" width="55" align="center" />
            <el-table-column label="敏感词" prop="word" align="center" />
            <el-table-column label="类型" align="center">
              <template #default="{ row }">
                <el-tag :type="row.wordType === 'CUSTOM' ? 'primary' : 'success'">
                  {{ row.wordType === 'CUSTOM' ? '自定义' : '官方' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="状态" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'danger'">
                  {{ row.status === 'ACTIVE' ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="创建时间" align="center" prop="createTime" width="180" />
            <el-table-column label="操作" width="120" align="center">
              <template #default="{ row }">
                <el-button
                  type="danger"
                  link
                  icon="Delete"
                  @click="handleDeleteWord(row)"
                >
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <!-- 右侧：白名单管理 + 测试 -->
      <el-col :span="10">
        <!-- 测试区域 -->
        <el-card class="box-card mb-4">
          <template #header>
            <div class="card-header">
              <span>敏感词测试</span>
            </div>
          </template>
          <el-input
            v-model="testText"
            type="textarea"
            :rows="3"
            placeholder="请输入要测试的文本"
          />
          <div class="mt-3">
            <el-button type="primary" @click="handleTest">检测</el-button>
            <el-button @click="testResult = ''">清空</el-button>
          </div>
          <div v-if="testResult" class="mt-3">
            <el-alert
              :type="testResult.includes('包含') ? 'warning' : 'success'"
              :title="testResult"
              show-icon
            />
          </div>
        </el-card>

        <!-- 白名单管理 -->
        <el-card class="box-card">
          <template #header>
            <div class="card-header">
              <span>白名单管理</span>
              <el-button type="primary" icon="Plus" @click="handleAddWhitelist">新增</el-button>
            </div>
          </template>

          <el-table
            v-loading="whitelistLoading"
            :data="whitelist"
            style="width: 100%"
            max-height="300"
          >
            <el-table-column label="白名单词" prop="word" align="center" />
            <el-table-column label="原因" prop="reason" align="center" show-overflow-tooltip />
            <el-table-column label="操作" width="100" align="center">
              <template #default="{ row }">
                <el-button
                  type="danger"
                  link
                  icon="Delete"
                  @click="handleDeleteWhitelist(row)"
                >
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 新增敏感词对话框 -->
    <el-dialog
      v-model="wordDialogVisible"
      title="新增敏感词"
      width="500px"
      append-to-body
    >
      <el-form
        ref="wordFormRef"
        :model="wordForm"
        :rules="wordRules"
        label-width="100px"
      >
        <el-form-item label="敏感词" prop="word">
          <el-input v-model="wordForm.word" placeholder="请输入敏感词" />
        </el-form-item>
        <el-form-item label="类型" prop="wordType">
          <el-select v-model="wordForm.wordType" placeholder="请选择类型">
            <el-option label="自定义" value="CUSTOM" />
            <el-option label="官方" value="OFFICIAL" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="wordForm.status">
            <el-radio value="ACTIVE">启用</el-radio>
            <el-radio value="DISABLED">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="wordDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitLoading" @click="submitWordForm">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 批量导入对话框 -->
    <el-dialog
      v-model="batchDialogVisible"
      title="批量导入敏感词"
      width="500px"
      append-to-body
    >
      <el-form label-width="100px">
        <el-form-item label="敏感词">
          <el-input
            v-model="batchWords"
            type="textarea"
            :rows="6"
            placeholder="请输入敏感词，每行一个"
          />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="batchWordType" placeholder="请选择类型">
            <el-option label="自定义" value="CUSTOM" />
            <el-option label="官方" value="OFFICIAL" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="batchDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="batchLoading" @click="submitBatchWords">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 新增白名单对话框 -->
    <el-dialog
      v-model="whitelistDialogVisible"
      title="新增白名单"
      width="500px"
      append-to-body
    >
      <el-form
        ref="whitelistFormRef"
        :model="whitelistForm"
        :rules="whitelistRules"
        label-width="100px"
      >
        <el-form-item label="白名单词" prop="word">
          <el-input v-model="whitelistForm.word" placeholder="请输入白名单词" />
        </el-form-item>
        <el-form-item label="原因" prop="reason">
          <el-input
            v-model="whitelistForm.reason"
            type="textarea"
            placeholder="请输入添加原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="whitelistDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="whitelistSubmitLoading" @click="submitWhitelistForm">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import {
  getSensitiveWordListApi,
  addSensitiveWordApi,
  batchAddSensitiveWordApi,
  deleteSensitiveWordApi,
  batchDeleteSensitiveWordApi,
  getWhitelistApi,
  addWhitelistApi,
  deleteWhitelistApi,
  reloadSensitiveWordApi,
  testSensitiveWordApi
} from '@/api/system/sensitive'

// ==================== 敏感词相关 ====================
const wordLoading = ref(false)
const wordList = ref<any[]>([])
const selectedWordIds = ref<number[]>([])
const wordDialogVisible = ref(false)
const batchDialogVisible = ref(false)
const submitLoading = ref(false)
const batchLoading = ref(false)
const wordFormRef = ref<FormInstance>()

// 查询参数
const wordQuery = reactive({
  word: '',
  wordType: '',
  status: ''
})

// 表单对象
const wordForm = reactive({
  word: '',
  wordType: 'CUSTOM',
  status: 'ACTIVE'
})

// 批量导入
const batchWords = ref('')
const batchWordType = ref('CUSTOM')

// 表单校验规则
const wordRules = {
  word: [
    { required: true, message: '请输入敏感词', trigger: 'blur' }
  ],
  wordType: [
    { required: true, message: '请选择类型', trigger: 'change' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}

// 获取敏感词列表
const getWordList = async () => {
  wordLoading.value = true
  try {
    const { data } = await getSensitiveWordListApi()
    wordList.value = data || []
  } catch (error) {
  }
  wordLoading.value = false
}

// 搜索
const handleWordQuery = () => {
  // 前端过滤
  getWordList()
}

// 重置查询
const resetWordQuery = () => {
  wordQuery.word = ''
  wordQuery.wordType = ''
  wordQuery.status = ''
  getWordList()
}

// 选择变化
const handleWordSelectionChange = (selection: any[]) => {
  selectedWordIds.value = selection.map(item => item.id)
}

// 新增敏感词
const handleAddWord = () => {
  wordForm.word = ''
  wordForm.wordType = 'CUSTOM'
  wordForm.status = 'ACTIVE'
  wordDialogVisible.value = true
}

// 批量导入
const handleBatchAddWord = () => {
  batchWords.value = ''
  batchWordType.value = 'CUSTOM'
  batchDialogVisible.value = true
}

// 提交单个敏感词
const submitWordForm = async () => {
  if (!wordFormRef.value) return

  await wordFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        await addSensitiveWordApi(wordForm)
        ElMessage.success('新增成功')
        wordDialogVisible.value = false
        getWordList()
      } catch (error) {
      } finally {
        submitLoading.value = false
      }
    }
  })
}

// 提交批量导入
const submitBatchWords = async () => {
  if (!batchWords.value.trim()) {
    ElMessage.warning('请输入敏感词')
    return
  }

  const words = batchWords.value
    .split('\n')
    .map(w => w.trim())
    .filter(w => w.length > 0)
    .map(word => ({
      word,
      wordType: batchWordType.value,
      status: 'ACTIVE'
    }))

  if (words.length === 0) {
    ElMessage.warning('请输入有效的敏感词')
    return
  }

  batchLoading.value = true
  try {
    await batchAddSensitiveWordApi(words)
    ElMessage.success(`成功导入 ${words.length} 个敏感词`)
    batchDialogVisible.value = false
    getWordList()
  } catch (error) {
  } finally {
    batchLoading.value = false
  }
}

// 删除敏感词
const handleDeleteWord = (row: any) => {
  ElMessageBox.confirm(
    `确定要删除敏感词"${row.word}"吗？`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteSensitiveWordApi(row.id)
      ElMessage.success('删除成功')
      getWordList()
    } catch (error) {
    }
  })
}

// 批量删除敏感词
const handleBatchDeleteWord = () => {
  if (selectedWordIds.value.length === 0) {
    ElMessage.warning('请选择要删除的记录')
    return
  }

  ElMessageBox.confirm(
    `确定要删除选中的 ${selectedWordIds.value.length} 条记录吗？`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await batchDeleteSensitiveWordApi(selectedWordIds.value)
      ElMessage.success('删除成功')
      getWordList()
    } catch (error) {
    }
  })
}

// 刷新词库
const handleReload = async () => {
  try {
    await reloadSensitiveWordApi()
    ElMessage.success('词库刷新成功')
  } catch (error) {
  }
}

// ==================== 测试相关 ====================
const testText = ref('')
const testResult = ref('')

const handleTest = async () => {
  if (!testText.value.trim()) {
    ElMessage.warning('请输入要测试的文本')
    return
  }
  try {
    const { data } = await testSensitiveWordApi(testText.value)
    testResult.value = data ? '⚠️ 文本包含敏感词' : '✅ 文本正常，不包含敏感词'
  } catch (error) {
  }
}

// ==================== 白名单相关 ====================
const whitelistLoading = ref(false)
const whitelist = ref<any[]>([])
const whitelistDialogVisible = ref(false)
const whitelistSubmitLoading = ref(false)
const whitelistFormRef = ref<FormInstance>()

const whitelistForm = reactive({
  word: '',
  reason: ''
})

const whitelistRules = {
  word: [
    { required: true, message: '请输入白名单词', trigger: 'blur' }
  ]
}

// 获取白名单列表
const getWhitelistList = async () => {
  whitelistLoading.value = true
  try {
    const { data } = await getWhitelistApi()
    whitelist.value = data || []
  } catch (error) {
  }
  whitelistLoading.value = false
}

// 新增白名单
const handleAddWhitelist = () => {
  whitelistForm.word = ''
  whitelistForm.reason = ''
  whitelistDialogVisible.value = true
}

// 提交白名单
const submitWhitelistForm = async () => {
  if (!whitelistFormRef.value) return

  await whitelistFormRef.value.validate(async (valid) => {
    if (valid) {
      whitelistSubmitLoading.value = true
      try {
        await addWhitelistApi(whitelistForm)
        ElMessage.success('新增成功')
        whitelistDialogVisible.value = false
        getWhitelistList()
      } catch (error) {
      } finally {
        whitelistSubmitLoading.value = false
      }
    }
  })
}

// 删除白名单
const handleDeleteWhitelist = (row: any) => {
  ElMessageBox.confirm(
    `确定要删除白名单"${row.word}"吗？`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteWhitelistApi(row.id)
      ElMessage.success('删除成功')
      getWhitelistList()
    } catch (error) {
    }
  })
}

// 初始化
getWordList()
getWhitelistList()
</script>

<style scoped>
.sensitive-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 20px;
}

.mb-4 {
  margin-bottom: 20px;
}

.mt-3 {
  margin-top: 12px;
}
</style>
