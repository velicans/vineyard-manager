export default function StageTimeline({ harvest, pressing, bottling }) {
  const stages = [
    { label: 'Cules', data: harvest, detail: harvest ? `${harvest.quantityKg} kg — ${harvest.date}` : null },
    { label: 'Stors', data: pressing, detail: pressing ? `${pressing.mustLiters} L (randament: ${(pressing.yieldRatio * 100).toFixed(1)}%)` : null },
    { label: 'Îmbuteliere', data: bottling, detail: bottling ? `${bottling.bottleCount} sticle × ${bottling.bottleVolume === 'L075' ? '0.75L' : '1.5L'}` : null },
  ]

  return (
    <div className="flex gap-4 mb-6">
      {stages.map((s, i) => (
        <div key={i} className={`flex-1 rounded-lg p-3 ${s.data ? 'bg-green-50 border border-green-200' : 'bg-gray-50 border border-gray-200'}`}>
          <div className={`font-medium ${s.data ? 'text-green-700' : 'text-gray-400'}`}>{s.label}</div>
          {s.detail && <div className="text-sm text-gray-600 mt-1">{s.detail}</div>}
          {!s.data && <div className="text-xs text-gray-400 mt-1">neînregistrat</div>}
        </div>
      ))}
    </div>
  )
}
