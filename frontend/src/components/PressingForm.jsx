import { useState } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { recordPressing } from '../api/batches'

export default function PressingForm({ batchId }) {
  const qc = useQueryClient()
  const [date, setDate] = useState('')
  const [mustLiters, setMustLiters] = useState('')
  const [error, setError] = useState(null)

  const mutation = useMutation({
    mutationFn: (data) => recordPressing(batchId, data),
    onSuccess: () => qc.invalidateQueries(['batch', batchId]),
    onError: (err) => setError(err.message),
  })

  const submit = (e) => {
    e.preventDefault()
    mutation.mutate({ date, mustLiters: parseFloat(mustLiters) })
  }

  return (
    <form onSubmit={submit} className="space-y-3">
      <h3 className="font-semibold text-lg">Înregistrează Stors</h3>
      <div>
        <label className="block text-sm font-medium mb-1">Data</label>
        <input type="date" value={date} onChange={e => setDate(e.target.value)} required className="border rounded px-3 py-2 w-full" />
      </div>
      <div>
        <label className="block text-sm font-medium mb-1">Must obținut (litri)</label>
        <input type="number" step="0.01" value={mustLiters} onChange={e => setMustLiters(e.target.value)} required className="border rounded px-3 py-2 w-full" />
      </div>
      {error && <p className="text-red-600 text-sm">{error}</p>}
      <button type="submit" className="bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600" disabled={mutation.isPending}>
        {mutation.isPending ? 'Se salvează...' : 'Salvează'}
      </button>
    </form>
  )
}
