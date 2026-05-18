import { useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { getBatch, getHarvest, getPressing, getBottling } from '../api/batches'
import StageTimeline from '../components/StageTimeline'
import HarvestForm from '../components/HarvestForm'
import PressingForm from '../components/PressingForm'
import BottlingForm from '../components/BottlingForm'

export default function BatchDetail() {
  const { id } = useParams()
  const { data: batch, isLoading } = useQuery({ queryKey: ['batch', id], queryFn: () => getBatch(id) })
  const { data: harvest } = useQuery({ queryKey: ['harvest', id], queryFn: () => getHarvest(id) })
  const { data: pressing } = useQuery({ queryKey: ['pressing', id], queryFn: () => getPressing(id) })
  const { data: bottling } = useQuery({ queryKey: ['bottling', id], queryFn: () => getBottling(id) })

  if (isLoading) return <p>Se încarcă...</p>
  if (!batch) return <p>Lot negăsit.</p>

  return (
    <div>
      <h1 className="text-2xl font-bold mb-2">{batch.parcelName} — {batch.year}</h1>
      <StageTimeline harvest={harvest} pressing={pressing} bottling={bottling} />
      <div className="bg-white rounded-lg shadow p-6">
        {batch.status === 'HARVESTED' && !harvest && <HarvestForm batchId={id} />}
        {batch.status === 'HARVESTED' && harvest && <PressingForm batchId={id} />}
        {batch.status === 'PRESSED' && !pressing && <PressingForm batchId={id} />}
        {batch.status === 'PRESSED' && pressing && <BottlingForm batchId={id} />}
        {batch.status === 'BOTTLED' && <p className="text-green-600 font-medium">Lot finalizat.</p>}
      </div>
    </div>
  )
}
