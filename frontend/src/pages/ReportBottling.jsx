import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { getBottlingReport } from '../api/reports'

export default function ReportBottling() {
  const [year, setYear] = useState('')
  const { data = [], isLoading } = useQuery({
    queryKey: ['report', 'bottling', year],
    queryFn: () => getBottlingReport(year || undefined),
  })

  const volumeLabel = (v) => v === 'L075' ? '0.75 L' : '1.5 L'

  return (
    <div>
      <h1 className="text-2xl font-bold mb-4">Raport Îmbuteliere</h1>
      <input type="number" placeholder="Toți anii" value={year} onChange={e => setYear(e.target.value)} className="border rounded px-3 py-2 mb-4 w-32" />
      {isLoading ? <p>Se încarcă...</p> : (
        <table className="w-full bg-white rounded-lg shadow text-sm">
          <thead className="bg-gray-100"><tr>
            <th className="px-4 py-2 text-left">Volum sticlă</th>
            <th className="px-4 py-2 text-right">Total sticle</th>
          </tr></thead>
          <tbody>
            {data.map((r, i) => (
              <tr key={i} className="border-t">
                <td className="px-4 py-2">{volumeLabel(r.bottleVolume)}</td>
                <td className="px-4 py-2 text-right">{r.totalBottles}</td>
              </tr>
            ))}
            {data.length === 0 && <tr><td colSpan={2} className="px-4 py-3 text-gray-400">Fără date.</td></tr>}
          </tbody>
        </table>
      )}
    </div>
  )
}
