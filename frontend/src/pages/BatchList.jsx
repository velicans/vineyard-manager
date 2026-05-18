import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { useState } from 'react'
import { getBatches } from '../api/batches'

export default function BatchList() {
  const { data: batches = [], isLoading } = useQuery({ queryKey: ['batches'], queryFn: getBatches })
  const [yearFilter, setYearFilter] = useState('')
  const [statusFilter, setStatusFilter] = useState('')

  const filtered = batches
    .filter(b => !yearFilter || b.year === parseInt(yearFilter))
    .filter(b => !statusFilter || b.status === statusFilter)

  if (isLoading) return <p>Se încarcă...</p>

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold">Loturi</h1>
        <Link to="/batches/new" className="bg-green-600 text-white px-4 py-2 rounded hover:bg-green-700">+ Lot nou</Link>
      </div>
      <div className="flex gap-3 mb-4">
        <input
          type="number" placeholder="An" value={yearFilter}
          onChange={e => setYearFilter(e.target.value)}
          className="border rounded px-3 py-1 w-24"
        />
        <select value={statusFilter} onChange={e => setStatusFilter(e.target.value)} className="border rounded px-3 py-1">
          <option value="">Toate statusurile</option>
          <option value="HARVESTED">HARVESTED</option>
          <option value="PRESSED">PRESSED</option>
          <option value="BOTTLED">BOTTLED</option>
        </select>
      </div>
      <div className="bg-white rounded-lg shadow divide-y">
        {filtered.map(b => (
          <Link key={b.id} to={`/batches/${b.id}`} className="flex items-center justify-between px-4 py-3 hover:bg-gray-50">
            <span className="font-medium">{b.parcelName}</span>
            <span className="text-gray-500">{b.year}</span>
            <span className={`text-xs px-2 py-1 rounded-full ${statusColor(b.status)}`}>{b.status}</span>
          </Link>
        ))}
        {filtered.length === 0 && <p className="px-4 py-3 text-gray-400">Niciun lot găsit.</p>}
      </div>
    </div>
  )
}

function statusColor(status) {
  if (status === 'BOTTLED') return 'bg-purple-100 text-purple-700'
  if (status === 'PRESSED') return 'bg-blue-100 text-blue-700'
  return 'bg-yellow-100 text-yellow-700'
}
