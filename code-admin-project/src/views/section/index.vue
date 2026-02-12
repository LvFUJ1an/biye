<template>
  <div class="section-container">
    <el-card class="section-card">
      <template #header>
        <div class="card-header">
          <span class="header-title">板块管理</span>
          <div class="header-operations">
            <el-input
              v-model="queryParams.name"
              placeholder="输入板块名称搜索"
              clearable
              style="width: 250px"
              @keyup.enter="handleQuery"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-button type="primary" @click="handleQuery">
              <el-icon><Search /></el-icon>搜索
            </el-button>
            <el-button @click="resetQuery">
              <el-icon><Refresh /></el-icon>重置
            </el-button>
            <el-button type="success" @click="handleAdd">
              <el-icon><Plus /></el-icon>新增板块
            </el-button>
          </div>
        </div>
      </template>
      
      <div class="section-content">
        <!-- 板块列表 -->
        <el-table
          v-loading="loading"
          :data="sectionList"
          style="width: 100%"
          border
          stripe
        >
          <el-table-column type="index" label="#" width="60" align="center" />
          <el-table-column prop="id" label="板块ID" width="80" align="center" />
          <el-table-column prop="name" label="板块名称" width="180" />
          <el-table-column prop="description" label="板块描述" min-width="250" show-overflow-tooltip />
          <!-- <el-table-column prop="adminId" label="创建人ID" width="100" align="center" /> -->
          <el-table-column prop="createTime" label="创建时间" width="170">
            <template #default="scope">
              {{ formatDate(scope.row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" align="center">
            <template #default="scope">
              <el-button
                type="primary"
                link
                @click="handleEdit(scope.row)"
              >
                编辑
              </el-button>
              <el-button
                type="danger"
                link
                @click="handleDelete(scope.row)"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="queryParams.current"
            v-model:page-size="queryParams.size"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </el-card>
    
    <!-- 添加/编辑板块对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '新增板块' : '编辑板块'"
      width="500px"
      destroy-on-close
    >
      <el-form
        ref="sectionFormRef"
        :model="sectionForm"
        :rules="sectionRules"
        label-width="80px"
      >
        <el-form-item label="名称" prop="name">
          <el-input v-model="sectionForm.name" placeholder="请输入板块名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="sectionForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入板块描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm" :loading="submitLoading">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getSectionList, addSection, updateSection, deleteSection, getSectionById } from '../../api/section'
import { message, confirm } from '../../utils/message'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'

// 定义板块类型
interface Section {
  id: number
  name: string
  description: string
  adminId: number
  createTime: string
}

// 加载状态
const loading = ref(false)
// 板块列表
const sectionList = ref<Section[]>([])
// 总数
const total = ref(0)
// 对话框可见性
const dialogVisible = ref(false)
// 对话框类型：add-新增，edit-编辑
const dialogType = ref<'add' | 'edit'>('add')
// 表单引用
const sectionFormRef = ref<FormInstance>()
// 保存原始数据用于提交完整的板块信息
const currentSectionData = ref<Section | null>(null)

// 查询参数
const queryParams = reactive({
  name: '',
  current: 1,
  size: 10
})

// 表单数据
const sectionForm = reactive({
  id: 0,
  name: '',
  description: ''
})

// 表单校验规则
const sectionRules = reactive<FormRules>({
  name: [
    { required: true, message: '请输入板块名称', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入板块描述', trigger: 'blur' },
    { min: 2, max: 200, message: '长度在 2 到 200 个字符', trigger: 'blur' }
  ]
})

// 表单提交loading
const submitLoading = ref(false)

// 获取板块列表
const getList = async () => {
  loading.value = true
  try {
    const res = await getSectionList({
      name: queryParams.name,
      current: queryParams.current,
      size: queryParams.size
    })
    
    if (res.code === 0) {
      sectionList.value = res.data.records
      total.value = res.data.total
    } else {
      message.error(res.message || '获取板块列表失败')
    }
  } catch (error: any) {
    message.error(error.message || '获取板块列表失败')
  } finally {
    loading.value = false
  }
}

// 格式化日期
const formatDate = (dateStr: string) => {
  if (!dateStr) return '未知'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 搜索
const handleQuery = () => {
  queryParams.current = 1
  getList()
}

// 重置搜索
const resetQuery = () => {
  queryParams.name = ''
  queryParams.current = 1
  getList()
}

// 新增板块
const handleAdd = () => {
  dialogType.value = 'add'
  sectionForm.id = 0
  sectionForm.name = ''
  sectionForm.description = ''
  // 清空当前选中的板块数据
  currentSectionData.value = null
  dialogVisible.value = true
}

// 编辑板块
const handleEdit = async (row: Section) => {
  dialogType.value = 'edit'
  
  // 加载最新的板块数据
  try {
    const res = await getSectionById(row.id)
    
    if (res.code === 0 && res.data) {
      // 填充表单数据
      sectionForm.id = res.data.id
      sectionForm.name = res.data.name
      sectionForm.description = res.data.description
      // 保存原始数据用于提交完整的板块信息
      currentSectionData.value = { ...res.data }
      
      dialogVisible.value = true
    } else {
      message.error(res.message || '获取板块详情失败')
    }
  } catch (error: any) {
    message.error(error.message || '获取板块详情失败')
  }
}

// 删除板块
const handleDelete = async (row: Section) => {
  // 确认操作
  const confirmed = await confirm.show(
    `确定要删除板块 "${row.name}" 吗？此操作不可恢复！`,
    '删除板块确认',
    'error',
    '确定删除',
    '取消'
  )
  
  if (!confirmed) {
    return
  }
  
  // 设置删除时的loading状态
  loading.value = true
  
  try {
    const res = await deleteSection(row.id)
    
    if (res.code === 0) {
      message.success(`已成功删除板块 "${row.name}"`)
      
      // 如果当前是最后一页且只有一条数据，删除后应该跳转到前一页
      if (
        queryParams.current > 1 && 
        sectionList.value.length === 1
      ) {
        queryParams.current--
      }
      
      // 重新加载列表数据
      getList()
    } else {
      message.error(res.message || '删除板块失败')
      loading.value = false
    }
  } catch (error: any) {
    message.error(error.message || '删除板块失败，请稍后重试')
    loading.value = false
  }
}

// 提交表单
const submitForm = async () => {
  if (!sectionFormRef.value) return
  
  await sectionFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    // 开始提交loading
    submitLoading.value = true
    
    try {
      let res
      
      if (dialogType.value === 'add') {
        // 新增
        res = await addSection({
          name: sectionForm.name,
          description: sectionForm.description
        })
        
        if (res.code === 0) {
          message.success('新增板块成功')
        } else {
          message.error(res.message || '新增板块失败')
          submitLoading.value = false
          return
        }
      } else {
        // 编辑 - 需要提交完整的板块数据
        res = await updateSection({
          id: sectionForm.id,
          name: sectionForm.name,
          description: sectionForm.description,
          adminId: currentSectionData.value?.adminId,
          createTime: currentSectionData.value?.createTime
        })
        
        if (res.code === 0) {
          message.success('更新板块成功')
        } else {
          message.error(res.message || '更新板块失败')
          submitLoading.value = false
          return
        }
      }
      
      // 关闭对话框
      dialogVisible.value = false
      // 重置提交loading状态
      submitLoading.value = false
      // 重新加载数据
      getList()
    } catch (error: any) {
      message.error(error.message || '操作失败，请稍后重试')
      submitLoading.value = false
    }
  })
}

// 页面大小变化
const handleSizeChange = (size: number) => {
  queryParams.size = size
  getList()
}

// 页码变化
const handleCurrentChange = (current: number) => {
  queryParams.current = current
  getList()
}

// 页面初始化时获取数据
onMounted(() => {
  getList()
})
</script>

<style scoped>
.section-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.section-card {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-title {
  font-size: 18px;
  font-weight: bold;
}

.header-operations {
  display: flex;
  gap: 10px;
}

.section-content {
  padding: 20px 0;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style> 