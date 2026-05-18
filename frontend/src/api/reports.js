import client from './client'

export const getHarvestReport = (year) => client.get('/reports/harvest', { params: { year } }).then(r => r.data)
export const getPressingReport = (year) => client.get('/reports/pressing', { params: { year } }).then(r => r.data)
export const getBottlingReport = (year) => client.get('/reports/bottling', { params: { year } }).then(r => r.data)
