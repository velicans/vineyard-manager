import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { getParcels } from '../api/parcels'
import { createBatch } from '../api/batches'

export default function BatchNew() {
  const navigate = useNavigate()
  const qc = useQueryClient()
  const { data: parcels = [] } = useQuery({ queryKey: ['parcels'], queryFn: getParcels })
  const [parcelId, setParcelId] = useState('')
  const [year, setYear] = useState(new Date().getFullYear())
  const [error, setError] = useState(null)

  const mutation = useMutation({
    mutationFn: createBatch,
    onSuccess: (batch) => { qc.invalidateQueries(['batches']); navigate(`/batches/${batch.id}`) },
    onError: (err) => setError(err.message),
  })

  const submit = (e) => {
    e.preventDefault()
    if (!parcelId) { setError('Selectează o parcelă'); return }
    mutation.mutate({ parcelId, year: parseInt(year) })
  }

  return (
    <div className="max-w-md">
      <h1 className="text-2xl font-bold mb-6">Lot nou</h1>
      <form onSubmit={submit} className="bg-white rounded-lg shadow p-6 space-y-4">
        <div>
          <label className="block text-sm font-medium mb-1">Parcelă</label>
          <select value={parcelId} onChange={e => setParcelId(e.target.value)} className="w-full border rounded px-3 py-2">
            <option value="">-- Selectează --</option>
            {parcels.map(p => <option key={p.id} value={p.id}>{p.name} ({p.grapeVariety})</option>)}
          </select>
        </div>
        <div>
          <label className="block text-sm font-medium mb-1">An</label>
          <input type="number" value={year} onChange={e => setYear(e.target.value)} className="w-full border rounded px-3 py-2" />
        </div>
        {error && <p className="text-red-600 text-sm">{error}</p>}
        <button type="submit" className="w-full bg-green-600 text-white py-2 rounded hover:bg-green-700" disabled={mutation.isPending}>
          {mutation.isPending ? 'Se creează...' : 'Creează lot'}
        </button>
      </form>
    </div>
  )
}
