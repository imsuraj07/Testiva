export default function AboutUsPage() {
  return (
    <div className="py-20 max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
      <div className="text-center mb-16">
        <h1 className="text-4xl md:text-5xl font-bold text-white mb-6">About <span className="text-primary-light">Testiva</span></h1>
        <p className="text-xl text-text-secondary leading-relaxed">
          Empowering education through technology. We build the tools that make assessments fair, fast, and insightful.
        </p>
      </div>

      <div className="glass-card p-8 md:p-12 mb-12">
        <h2 className="text-2xl font-bold text-white mb-4">Our Mission</h2>
        <p className="text-text-secondary mb-8 leading-relaxed">
          Testiva was born out of the need for a reliable, modern online examination system. Traditional methods are prone to errors, slow to grade, and offer little in terms of analytics. Our mission is to provide institutions and students with a seamless, secure, and intuitive platform to conduct and take exams.
        </p>

        <h2 className="text-2xl font-bold text-white mb-4">What We Do</h2>
        <ul className="space-y-4 text-text-secondary">
          <li className="flex items-start gap-3">
            <span className="w-6 h-6 rounded-full bg-primary/20 text-primary-light flex items-center justify-center shrink-0 mt-0.5">✓</span>
            <span><strong>Secure Assessments:</strong> We provide a distraction-free environment for students to take tests securely.</span>
          </li>
          <li className="flex items-start gap-3">
            <span className="w-6 h-6 rounded-full bg-primary/20 text-primary-light flex items-center justify-center shrink-0 mt-0.5">✓</span>
            <span><strong>Instant Results:</strong> Our automated grading system delivers results the moment a test is submitted.</span>
          </li>
          <li className="flex items-start gap-3">
            <span className="w-6 h-6 rounded-full bg-primary/20 text-primary-light flex items-center justify-center shrink-0 mt-0.5">✓</span>
            <span><strong>Comprehensive Analytics:</strong> Educators get detailed insights into student performance.</span>
          </li>
        </ul>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
        <div className="glass-card p-8 text-center">
          <div className="text-4xl font-bold text-white mb-2">10k+</div>
          <div className="text-text-secondary">Tests Conducted</div>
        </div>
        <div className="glass-card p-8 text-center">
          <div className="text-4xl font-bold text-white mb-2">50k+</div>
          <div className="text-text-secondary">Students Evaluated</div>
        </div>
      </div>
    </div>
  );
}
