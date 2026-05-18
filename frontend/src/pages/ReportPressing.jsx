import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { getPressingReport } from '../api/reports'

export default function ReportPressing() {
  const [year, setYear] = useState('')
  const { data = [], isLoading } = useQuery({
    queryKey: ['report', 'pressing', year],
    queryFn: () => getPressingReport(year || undefined),
  })

  return (
    <div>
      <h1 className="text-2xl font-bold mb-4">Raport Stors</h1>
      <input type="number" placeholder="Toți anii" value={year} onChange={e => setYear(e.target.value)} className="border rounded px-3 py-2 mb-4 w-32" />
      {isLoading ? <p>Se încarcă...</p> : (
        <table className="w-full bg-white rounded-lg shadow text-sm">
          <thead className="bg-gray-100"><tr>
            <th className="px-4 py-2 text-left">Parcelă</th>
            <th className="px-4 py-2 text-right">Total litri must</th>
            <th className="px-4 py-2 text-right">Randament mediu</th>
          </tr></thead>
          <tbody>
            {data.map((r, i) => (
              <tr key={i} className="border-t">
                <td className="px-4 py-2">{r.parcelName}</td>
                <td className="px-4 py-2 text-right">{r.totalMustLiters}</td>
                <td className="px-4 py-2 text-right">{(r.avgYieldRatio * 100).toFixed(1)}%</td>
              </tr>
            ))}
            {data.length === 0 && <tr><td colSpan={3} className="px-4 py-3 text-gray-400">Fără date.</td></tr>}
          </tbody>
        </table>
      )}
    </div>
  )
}
