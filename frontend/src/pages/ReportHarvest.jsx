import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { getHarvestReport } from '../api/reports'

export default function ReportHarvest() {
  const [year, setYear] = useState('')
  const { data = [], isLoading } = useQuery({
    queryKey: ['report', 'harvest', year],
    queryFn: () => getHarvestReport(year || undefined),
  })
  const years = Array.from({ length: 10 }, (_, i) => new Date().getFullYear() - i)

  return (
    <div>
      <h1 className="text-2xl font-bold mb-4">Raport Cules</h1>
      <select value={year} onChange={e => setYear(e.target.value)} className="border rounded px-3 py-2 mb-4">
        <option value="">Toți anii</option>
        {years.map(y => <option key={y} value={y}>{y}</option>)}
      </select>
      {isLoading ? <p>Se încarcă...</p> : (
        <table className="w-full bg-white rounded-lg shadow text-sm">
          <thead className="bg-gray-100"><tr>
            <th className="px-4 py-2 text-left">Parcelă</th>
            <th className="px-4 py-2 text-right">Total kg</th>
            <th className="px-4 py-2 text-right">Medie kg</th>
          </tr></thead>
          <tbody>
            {data.map((r, i) => (
              <tr key={i} className="border-t">
                <td className="px-4 py-2">{r.parcelName}</td>
                <td className="px-4 py-2 text-right">{r.totalKg}</td>
                <td className="px-4 py-2 text-right">{r.avgKg}</td>
              </tr>
            ))}
            {data.length === 0 && <tr><td colSpan={3} className="px-4 py-3 text-gray-400">Fără date.</td></tr>}
          </tbody>
        </table>
      )}
    </div>
  )
}
