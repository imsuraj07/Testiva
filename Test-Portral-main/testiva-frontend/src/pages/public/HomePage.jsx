import { Link } from 'react-router-dom';
import { HiOutlineLightningBolt, HiOutlineShieldCheck, HiOutlineChartBar, HiOutlineDesktopComputer } from 'react-icons/hi';

export default function HomePage() {
  const features = [
    {
      title: 'Real-time Assessments',
      desc: 'Conduct exams with zero latency. Live timer, auto-submit, and robust tracking.',
      icon: HiOutlineLightningBolt,
      color: 'from-amber-500 to-orange-600',
    },
    {
      title: 'Bank-grade Security',
      desc: 'Secure sessions, prevent cheating, and protect your question banks effortlessly.',
      icon: HiOutlineShieldCheck,
      color: 'from-blue-500 to-indigo-600',
    },
    {
      title: 'Instant Analytics',
      desc: 'Get immediate insights. See correct, wrong, and overall performance metrics instantly.',
      icon: HiOutlineChartBar,
      color: 'from-emerald-400 to-teal-500',
    },
    {
      title: 'Modern Experience',
      desc: 'A beautiful, distraction-free UI designed to help students focus and perform better.',
      icon: HiOutlineDesktopComputer,
      color: 'from-purple-500 to-pink-600',
    },
  ];

  return (
    <div className="flex flex-col">
      {/* Hero Section */}
      <section className="relative pt-32 pb-20 lg:pt-48 lg:pb-32 overflow-hidden">
        {/* Background Gradients */}
        <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[800px] h-[800px] bg-primary/20 rounded-full blur-[120px] pointer-events-none" />
        
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 relative z-10 text-center">
          <div className="inline-flex items-center gap-2 px-4 py-2 rounded-full bg-white/5 border border-white/10 text-primary-light mb-8">
            <span className="w-2 h-2 rounded-full bg-primary animate-pulse" />
            <span className="text-sm font-medium tracking-wide">The Next Generation of Assessments</span>
          </div>
          
          <h1 className="text-5xl md:text-7xl font-extrabold text-white tracking-tight mb-8">
            Evaluate brilliance with <br className="hidden md:block" />
            <span className="text-transparent bg-clip-text bg-gradient-to-r from-primary-light via-primary to-secondary">
              absolute precision.
            </span>
          </h1>
          
          <p className="max-w-2xl mx-auto text-lg md:text-xl text-text-secondary mb-10 leading-relaxed">
            Testiva is a modern, reliable, and lightning-fast platform to conduct online exams, manage question banks, and instantly analyze student performance.
          </p>
          
          <div className="flex flex-col sm:flex-row items-center justify-center gap-4">
            <Link to="/register" className="btn-primary text-lg px-8 py-4 rounded-full w-full sm:w-auto justify-center shadow-lg shadow-primary/25">
              Get Started for Free
            </Link>
            <Link to="/login" className="btn-outline text-lg px-8 py-4 rounded-full w-full sm:w-auto justify-center border-white/10 hover:bg-white/5">
              Admin Login
            </Link>
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section className="py-20 bg-surface/30 border-y border-white/5 relative z-10">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-16">
            <h2 className="text-3xl font-bold text-white mb-4">Why choose Testiva?</h2>
            <p className="text-text-secondary max-w-2xl mx-auto">
              Everything you need to run professional, secure, and highly scalable online assessments in one place.
            </p>
          </div>
          
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
            {features.map((feature, i) => (
              <div key={i} className="glass-card-hover p-8 relative overflow-hidden group">
                <div className={`absolute top-0 right-0 w-32 h-32 bg-gradient-to-br ${feature.color} opacity-10 blur-2xl group-hover:opacity-20 transition-opacity`} />
                <div className={`w-14 h-14 rounded-2xl bg-gradient-to-br ${feature.color} flex items-center justify-center mb-6 shadow-lg`}>
                  <feature.icon className="text-white" size={28} />
                </div>
                <h3 className="text-xl font-bold text-white mb-3">{feature.title}</h3>
                <p className="text-text-secondary leading-relaxed">{feature.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="py-32 relative z-10">
        <div className="max-w-4xl mx-auto px-4 text-center">
          <h2 className="text-4xl font-bold text-white mb-6">Ready to transform your assessments?</h2>
          <p className="text-xl text-text-secondary mb-10">
            Join thousands of educators and students using Testiva today.
          </p>
          <Link to="/register" className="btn-primary text-lg px-10 py-4 rounded-full inline-flex items-center gap-2">
            Create Student Account
          </Link>
        </div>
      </section>
    </div>
  );
}
