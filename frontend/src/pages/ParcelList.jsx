import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { getParcels, createParcel, deleteParcel } from '../api/parcels'

export default function ParcelList() {
  const qc = useQueryClient()
  const { data: parcels = [], isLoading } = useQuery({ queryKey: ['parcels'], queryFn: getParcels })
  const [showForm, setShowForm] = useState(false)
  const [name, setName] = useState('')
  const [grapeVariety, setGrapeVariety] = useState('')
  const [areaSqM, setAreaSqM] = useState('')
  const [formError, setFormError] = useState(null)

  const createMutation = useMutation({
    mutationFn: createParcel,
    onSuccess: () => {
      qc.invalidateQueries(['parcels'])
      setShowForm(false)
      setName('')
      setGrapeVariety('')
      setAreaSqM('')
      setFormError(null)
    },
    onError: (err) => setFormError(err.message),
  })

  const deleteMutation = useMutation({
    mutationFn: deleteParcel,
    onSuccess: () => qc.invalidateQueries(['parcels']),
  })

  const handleCreate = (e) => {
    e.preventDefault()
    createMutation.mutate({ name, grapeVariety, areaSqM: parseInt(areaSqM) })
  }

  const handleDelete = (id, parcelName) => {
    if (!window.confirm(`Ștergi parcela "${parcelName}"? Această acțiune nu poate fi anulată.`)) return
    deleteMutation.mutate(id)
  }

  if (isLoading) return <p>Se încarcă...</p>

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold">Parcele</h1>
        <button
          onClick={() => setShowForm(!showForm)}
          className="bg-green-600 text-white px-4 py-2 rounded hover:bg-green-700"
        >
          {showForm ? 'Anulează' : '+ Parcelă nouă'}
        </button>
      </div>

      {showForm && (
        <form onSubmit={handleCreate} className="bg-white rounded-lg shadow p-4 mb-6 space-y-3">
          <div className="grid grid-cols-3 gap-3">
            <div>
              <label className="block text-sm font-medium mb-1">Nume</label>
              <input
                type="text" value={name} onChange={e => setName(e.target.value)} required
                className="w-full border rounded px-3 py-2 text-sm"
              />
            </div>
            <div>
              <label className="block text-sm font-medium mb-1">Soi</label>
              <input
                type="text" value={grapeVariety} onChange={e => setGrapeVariety(e.target.value)} required
                className="w-full border rounded px-3 py-2 text-sm"
              />
            </div>
            <div>
              <label className="block text-sm font-medium mb-1">Suprafață (m²)</label>
              <input
                type="number" value={areaSqM} onChange={e => setAreaSqM(e.target.value)} required min="1"
                className="w-full border rounded px-3 py-2 text-sm"
              />
            </div>
          </div>
          {formError && <p className="text-red-600 text-sm">{formError}</p>}
          <button
            type="submit"
            className="bg-green-600 text-white px-4 py-2 rounded hover:bg-green-700 text-sm"
            disabled={createMutation.isPending}
          >
            {createMutation.isPending ? 'Se salvează...' : 'Salvează'}
          </button>
        </form>
      )}

      <div className="grid gap-4">
        {parcels.map(p => (
          <div key={p.id} className="bg-white rounded-lg shadow p-4 flex items-center justify-between">
            <div>
              <div className="font-semibold text-lg">{p.name}</div>
              <div className="text-gray-500">{p.grapeVariety}</div>
              <div className="text-sm text-gray-400">{p.areaSqM} m²</div>
            </div>
            <button
              onClick={() => handleDelete(p.id, p.name)}
              className="text-red-500 hover:text-red-700 text-sm px-3 py-1 border border-red-200 rounded hover:bg-red-50"
              disabled={deleteMutation.isPending}
            >
              Șterge
            </button>
          </div>
        ))}
        {parcels.length === 0 && <p className="text-gray-400">Nicio parcelă înregistrată.</p>}
      </div>
    </div>
  )
}
