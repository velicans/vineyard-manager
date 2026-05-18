import client from './client'

export const getParcels = () => client.get('/parcels').then(r => r.data)
export const getParcel = (id) => client.get(`/parcels/${id}`).then(r => r.data)
export const createParcel = (data) => client.post('/parcels', data).then(r => r.data)
export const deleteParcel = (id) => client.delete(`/parcels/${id}`)
