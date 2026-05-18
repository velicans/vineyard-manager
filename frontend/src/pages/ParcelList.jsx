import { useQuery } from '@tanstack/react-query'
import { getParcels } from '../api/parcels'

export default function ParcelList() {
  const { data: parcels = [], isLoading } = useQuery({ queryKey: ['parcels'], queryFn: getParcels })

  if (isLoading) return <p>Se încarcă...</p>

  return (
    <div>
      <h1 className="text-2xl font-bold mb-6">Parcele</h1>
      <div className="grid gap-4">
        {parcels.map(p => (
          <div key={p.id} className="bg-white rounded-lg shadow p-4">
            <div className="font-semibold text-lg">{p.name}</div>
            <div className="text-gray-500">{p.grapeVariety}</div>
            <div className="text-sm text-gray-400">{p.areaSqM} m²</div>
          </div>
        ))}
      </div>
    </div>
  )
}
