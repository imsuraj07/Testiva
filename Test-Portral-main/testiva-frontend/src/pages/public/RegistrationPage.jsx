import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { publicAPI } from '../../services/api';
import toast from 'react-hot-toast';
import { HiOutlineUserAdd, HiOutlineUpload } from 'react-icons/hi';

export default function RegistrationPage() {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    contactno: '',
    password: '',
    course: '',
    branch: '',
    year: '',
  });
  const [profilePic, setProfilePic] = useState(null);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      const data = new FormData();
      Object.keys(formData).forEach(key => data.append(key, formData[key]));
      if (profilePic) {
        data.append('profilePic', profilePic);
      }

      const res = await publicAPI.register(data);
      toast.success(res.data.message || 'Registration successful!');
      navigate('/login');
    } catch (err) {
      toast.error(err.response?.data?.error || 'Registration failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-[calc(100vh-160px)] flex items-center justify-center py-12 px-4">
      <div className="glass-card w-full max-w-2xl p-8">
        <div className="text-center mb-8">
          <div className="w-16 h-16 rounded-2xl bg-gradient-to-br from-primary to-secondary flex items-center justify-center mx-auto mb-4 shadow-lg shadow-primary/20">
            <HiOutlineUserAdd className="text-white" size={32} />
          </div>
          <h2 className="text-2xl font-bold text-white">Student Registration</h2>
          <p className="text-text-secondary mt-2">Create your account to start taking tests.</p>
        </div>

        <form onSubmit={handleSubmit} className="space-y-5">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
            <div>
              <label className="block text-sm font-medium text-text-secondary mb-1">Full Name</label>
              <input required className="input-field" value={formData.name} onChange={e => setFormData({...formData, name: e.target.value})} />
            </div>
            <div>
              <label className="block text-sm font-medium text-text-secondary mb-1">Email</label>
              <input required type="email" className="input-field" value={formData.email} onChange={e => setFormData({...formData, email: e.target.value})} />
            </div>
            <div>
              <label className="block text-sm font-medium text-text-secondary mb-1">Mobile Number</label>
              <input required className="input-field" value={formData.contactno} onChange={e => setFormData({...formData, contactno: e.target.value})} />
            </div>
            <div>
              <label className="block text-sm font-medium text-text-secondary mb-1">Password</label>
              <input required type="password" className="input-field" value={formData.password} onChange={e => setFormData({...formData, password: e.target.value})} />
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
            <div>
              <label className="block text-sm font-medium text-text-secondary mb-1">Course</label>
              <select required className="input-field" value={formData.course} onChange={e => setFormData({...formData, course: e.target.value})}>
                <option value="">Select Course</option>
                <option value="B.Tech">B.Tech</option>
                <option value="MCA">MCA</option>
                <option value="BCA">BCA</option>
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium text-text-secondary mb-1">Branch</label>
              <select required className="input-field" value={formData.branch} onChange={e => setFormData({...formData, branch: e.target.value})}>
                <option value="">Select Branch</option>
                <option value="Computer Science">Computer Science</option>
                <option value="Information Tech">Information Tech</option>
                <option value="Electronics">Electronics</option>
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium text-text-secondary mb-1">Year</label>
              <select required className="input-field" value={formData.year} onChange={e => setFormData({...formData, year: e.target.value})}>
                <option value="">Select Year</option>
                <option value="First Year">First Year</option>
                <option value="Second Year">Second Year</option>
                <option value="Third Year">Third Year</option>
                <option value="Final Year">Final Year</option>
              </select>
            </div>
          </div>

          <div>
            <label className="block text-sm font-medium text-text-secondary mb-1">Profile Picture (Optional)</label>
            <div className="flex items-center gap-4">
              <label className="cursor-pointer flex items-center justify-center gap-2 px-4 py-2 rounded-lg border border-white/10 bg-white/5 hover:bg-white/10 text-white transition-colors">
                <HiOutlineUpload size={18} /> Choose File
                <input type="file" className="hidden" accept="image/*" onChange={e => setProfilePic(e.target.files[0])} />
              </label>
              <span className="text-sm text-text-secondary truncate">{profilePic ? profilePic.name : 'No file chosen'}</span>
            </div>
          </div>

          <button type="submit" disabled={loading} className="btn-primary w-full justify-center py-3 mt-4">
            {loading ? <div className="spinner w-5 h-5 border-2" /> : 'Register Account'}
          </button>
        </form>

        <p className="text-center text-sm text-text-secondary mt-6">
          Already have an account? <Link to="/login" className="text-primary-light hover:underline font-medium">Log in</Link>
        </p>
      </div>
    </div>
  );
}
