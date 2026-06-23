import { useState, useEffect } from 'react';
import { adminAPI } from '../../services/api';
import { HiOutlineSearch } from 'react-icons/hi';

export default function ManageEnquiries() {
  const [enquiries, setEnquiries] = useState([]);
  const [search, setSearch] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    adminAPI.getEnquiries().then(res => { setEnquiries(res.data); setLoading(false); }).catch(() => setLoading(false));
  }, []);

  const filtered = enquiries.filter(e =>
    e.name?.toLowerCase().includes(search.toLowerCase()) ||
    e.email?.toLowerCase().includes(search.toLowerCase())
  );

  if (loading) return <div className="flex justify-center py-20"><div className="spinner" /></div>;

  return (
    <div className="space-y-4">
      <div className="flex flex-col sm:flex-row gap-3 items-start sm:items-center justify-between">
        <div className="relative w-full sm:w-80">
          <HiOutlineSearch className="absolute left-3.5 top-1/2 -translate-y-1/2 text-text-secondary" size={18} />
          <input type="text" className="input-field pl-11" placeholder="Search enquiries..." value={search} onChange={e => setSearch(e.target.value)} />
        </div>
        <span className="text-sm text-text-secondary">{filtered.length} enquiries</span>
      </div>

      <div className="grid gap-4">
        {filtered.map(e => (
          <div key={e.id} className="glass-card-hover p-5">
            <div className="flex items-start justify-between mb-3">
              <div>
                <h3 className="font-medium text-white">{e.name}</h3>
                <p className="text-xs text-text-secondary">{e.email} • {e.contactno}</p>
              </div>
              <span className={`status-badge ${e.status === 'PENDING' ? 'bg-amber-500/15 text-amber-400' : 'bg-green-500/15 text-green-400'}`}>
                <span className="w-1.5 h-1.5 rounded-full bg-current" />
                {e.status?.replace('_', ' ')}
              </span>
            </div>
            {e.subject && <p className="text-sm font-medium text-primary-light mb-1">Subject: {e.subject}</p>}
            <p className="text-sm text-text-secondary">{e.enquiryText}</p>
            <p className="text-xs text-text-secondary mt-3">{e.submittedAt ? new Date(e.submittedAt).toLocaleString() : ''}</p>
          </div>
        ))}
        {filtered.length === 0 && <p className="text-center py-8 text-text-secondary">No enquiries found</p>}
      </div>
    </div>
  );
}
