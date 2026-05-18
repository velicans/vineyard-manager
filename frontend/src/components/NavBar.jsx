import { Link, NavLink } from 'react-router-dom'

export default function NavBar() {
  const linkClass = ({ isActive }) =>
    `px-3 py-2 rounded text-sm font-medium ${isActive ? 'bg-green-700 text-white' : 'text-gray-300 hover:bg-gray-700 hover:text-white'}`

  return (
    <nav className="bg-gray-800 px-4 py-3 flex items-center gap-4">
      <Link to="/" className="text-white font-bold text-lg mr-4">Vineyard</Link>
      <NavLink to="/parcels" className={linkClass}>Parcele</NavLink>
      <NavLink to="/batches" className={linkClass}>Loturi</NavLink>
      <NavLink to="/reports/harvest" className={linkClass}>Raport Cules</NavLink>
      <NavLink to="/reports/pressing" className={linkClass}>Raport Stors</NavLink>
      <NavLink to="/reports/bottling" className={linkClass}>Raport Imbuteliere</NavLink>
    </nav>
  )
}
