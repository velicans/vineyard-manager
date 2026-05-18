import client from './client'

export const getBatches = () => client.get('/batches').then(r => r.data)
export const getBatch = (id) => client.get(`/batches/${id}`).then(r => r.data)
export const createBatch = (data) => client.post('/batches', data).then(r => r.data)
export const recordHarvest = (batchId, data) => client.post(`/batches/${batchId}/harvest`, data).then(r => r.data)
export const recordPressing = (batchId, data) => client.post(`/batches/${batchId}/pressing`, data).then(r => r.data)
export const recordBottling = (batchId, data) => client.post(`/batches/${batchId}/bottling`, data).then(r => r.data)
export const getHarvest = (batchId) => client.get(`/batches/${batchId}/harvest`).then(r => r.data)
export const getPressing = (batchId) => client.get(`/batches/${batchId}/pressing`).then(r => r.data)
export const getBottling = (batchId) => client.get(`/batches/${batchId}/bottling`).then(r => r.data)
export const deleteBatch = (id) => client.delete(`/batches/${id}`)
export const deleteHarvest = (batchId) => client.delete(`/batches/${batchId}/harvest`)
export const deletePressing = (batchId) => client.delete(`/batches/${batchId}/pressing`)
