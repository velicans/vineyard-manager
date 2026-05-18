import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { getBatches } from '../api/batches'
import { getParcels } from '../api/parcels'

export default function Dashboard() {
  const { data: batches = [] } = useQuery({ queryKey: ['batches'], queryFn: getBatches })
  const { data: parcels = [] } = useQuery({ queryKey: ['parcels'], queryFn: getParcels })

  const recent = batches.slice(-5).reverse()

  return (
    <div>
      <h1 className="text-2xl font-bold mb-6">Dashboard</h1>
      <div className="grid grid-cols-3 gap-4 mb-8">
        <div className="bg-white rounded-lg p-4 shadow">
          <div className="text-3xl font-bold text-green-600">{parcels.length}</div>
          <div className="text-gray-500">Parcele</div>
        </div>
        <div className="bg-white rounded-lg p-4 shadow">
          <div className="text-3xl font-bold text-blue-600">{batches.length}</div>
          <div className="text-gray-500">Loturi totale</div>
        </div>
        <div className="bg-white rounded-lg p-4 shadow">
          <div className="text-3xl font-bold text-purple-600">
            {batches.filter(b => b.status === 'BOTTLED').length}
          </div>
          <div className="text-gray-500">Loturi finalizate</div>
        </div>
      </div>
      <h2 className="text-lg font-semibold mb-3">Ultimele loturi</h2>
      <div className="bg-white rounded-lg shadow divide-y">
        {recent.map(b => (
          <Link key={b.id} to={`/batches/${b.id}`} className="flex items-center justify-between px-4 py-3 hover:bg-gray-50">
            <span>{b.parcelName} — {b.year}</span>
            <span className={`text-xs px-2 py-1 rounded-full ${statusColor(b.status)}`}>{b.status}</span>
          </Link>
        ))}
        {recent.length === 0 && <p className="px-4 py-3 text-gray-400">Niciun lot înregistrat.</p>}
      </div>
      <Link to="/batches/new" className="mt-6 inline-block bg-green-600 text-white px-4 py-2 rounded hover:bg-green-700">
        + Lot nou
      </Link>
    </div>
  )
}

function statusColor(status) {
  if (status === 'BOTTLED') return 'bg-purple-100 text-purple-700'
  if (status === 'PRESSED') return 'bg-blue-100 text-blue-700'
  return 'bg-yellow-100 text-yellow-700'
}
