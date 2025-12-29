import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getDatasourceList } from '@/api/datasource'

export const useDatasourceStore = defineStore('datasource', () => {
  const dataSources = ref([])
  const loading = ref(false)

  const loadDataSources = async () => {
    loading.value = true
    try {
      const res = await getDatasourceList()
      dataSources.value = res.data.map(ds => ({
        ...ds,
        status: 'success'
      }))
      return dataSources.value
    } finally {
      loading.value = false
    }
  }

  return {
    dataSources,
    loading,
    loadDataSources
  }
})
