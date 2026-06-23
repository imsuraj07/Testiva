import { useState } from 'react';
import { publicAPI } from '../../services/api';
import toast from 'react-hot-toast';
import { HiOutlineMail, HiOutlinePhone, HiOutlineLocationMarker } from 'react-icons/hi';

export default function ContactUsPage() {
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    contactno: '',
    subject: '',
    message: '',
  });
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      const res = await publicAPI.contact(formData);
      toast.success(res.data.message || 'Enquiry submitted!');
      setFormData({ name: '', email: '', contactno: '', subject: '', message: '' });
    } catch (err) {
      toast.error(err.response?.data?.error || 'Failed to submit enquiry');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="py-20 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div className="text-center mb-16">
        <h1 className="text-4xl font-bold text-white mb-4">Contact <span className="text-primary-light">Us</span></h1>
        <p className="text-lg text-text-secondary">Have questions? We're here to help.</p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Contact Info */}
        <div className="space-y-6">
          <div className="glass-card p-6 flex items-start gap-4">
            <div className="w-12 h-12 rounded-xl bg-primary/20 flex items-center justify-center text-primary-light shrink-0">
              <HiOutlinePhone size={24} />
            </div>
            <div>
              <h3 className="text-white font-semibold mb-1">Phone</h3>
              <p className="text-text-secondary">+91 98765 43210</p>
            </div>
          </div>
          
          <div className="glass-card p-6 flex items-start gap-4">
            <div className="w-12 h-12 rounded-xl bg-secondary/20 flex items-center justify-center text-secondary shrink-0">
              <HiOutlineMail size={24} />
            </div>
            <div>
              <h3 className="text-white font-semibold mb-1">Email</h3>
              <p className="text-text-secondary">support@testiva.edu.in</p>
            </div>
          </div>

          <div className="glass-card p-6 flex items-start gap-4">
            <div className="w-12 h-12 rounded-xl bg-emerald-500/20 flex items-center justify-center text-emerald-400 shrink-0">
              <HiOutlineLocationMarker size={24} />
            </div>
            <div>
              <h3 className="text-white font-semibold mb-1">Office</h3>
              <p className="text-text-secondary">Sector-21, Pune, Maharashtra</p>
            </div>
          </div>
        </div>

        {/* Form */}
        <div className="lg:col-span-2 glass-card p-8">
          <h2 className="text-xl font-bold text-white mb-6">Send us a message</h2>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-text-secondary mb-1">Full Name</label>
                <input
                  required
                  className="input-field"
                  placeholder="John Doe"
                  value={formData.name}
                  onChange={e => setFormData({...formData, name: e.target.value})}
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-text-secondary mb-1">Mobile Number</label>
                <input
                  required
                  className="input-field"
                  placeholder="9876543210"
                  value={formData.contactno}
                  onChange={e => setFormData({...formData, contactno: e.target.value})}
                />
              </div>
            </div>
            
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-text-secondary mb-1">Email Address</label>
                <input
                  required
                  type="email"
                  className="input-field"
                  placeholder="you@example.com"
                  value={formData.email}
                  onChange={e => setFormData({...formData, email: e.target.value})}
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-text-secondary mb-1">Subject</label>
                <input
                  required
                  className="input-field"
                  placeholder="E.g. Account Issue"
                  value={formData.subject}
                  onChange={e => setFormData({...formData, subject: e.target.value})}
                />
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-text-secondary mb-1">Message</label>
              <textarea
                required
                rows={4}
                className="input-field"
                placeholder="How can we help you?"
                value={formData.message}
                onChange={e => setFormData({...formData, message: e.target.value})}
              />
            </div>

            <button type="submit" disabled={loading} className="btn-primary w-full justify-center py-3">
              {loading ? <div className="spinner w-5 h-5 border-2" /> : 'Submit Enquiry'}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}
