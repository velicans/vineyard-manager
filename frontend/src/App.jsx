import { Routes, Route } from 'react-router-dom'
import Layout from './components/Layout'
import Dashboard from './pages/Dashboard'
import ParcelList from './pages/ParcelList'
import BatchList from './pages/BatchList'
import BatchNew from './pages/BatchNew'
import BatchDetail from './pages/BatchDetail'
import ReportHarvest from './pages/ReportHarvest'
import ReportPressing from './pages/ReportPressing'
import ReportBottling from './pages/ReportBottling'

export default function App() {
  return (
    <Layout>
      <Routes>
        <Route path="/" element={<Dashboard />} />
        <Route path="/parcels" element={<ParcelList />} />
        <Route path="/batches" element={<BatchList />} />
        <Route path="/batches/new" element={<BatchNew />} />
        <Route path="/batches/:id" element={<BatchDetail />} />
        <Route path="/reports/harvest" element={<ReportHarvest />} />
        <Route path="/reports/pressing" element={<ReportPressing />} />
        <Route path="/reports/bottling" element={<ReportBottling />} />
      </Routes>
    </Layout>
  )
}
