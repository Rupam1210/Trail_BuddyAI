import { useEffect, useState } from "react";
import {
  ArrowUpRight, Check, Compass, Leaf, MapPin, Printer, ShieldCheck,
  Sparkles, Sprout, Backpack, Footprints, Bird, Trees, Activity
} from "lucide-react";

const activities = [
  { value: "hiking", label: "Hiking", icon: Footprints },
  { value: "walking", label: "Walking", icon: Trees },
  { value: "gardening", label: "Gardening", icon: Sprout },
  { value: "birding", label: "Birdwatching", icon: Bird },
  { value: "running", label: "Running", icon: Activity }
];

const initialForm = {
  place: "",
  activity: "hiking",
  duration: "120",
  level: "beginner",
  interests: ""
};

export default function App() {
  const [form, setForm] = useState(initialForm);
  const [health, setHealth] = useState(null);
  const [plan, setPlan] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [packed, setPacked] = useState({});
  const [completed, setCompleted] = useState({});

  useEffect(() => {
    fetch("/api/health")
      .then((r) => r.json())
      .then(setHealth)
      .catch(() => setHealth({ ok: false, ollama: false, modelAvailable: false }));
  }, []);

  const update = (event) => {
    setForm((old) => ({ ...old, [event.target.name]: event.target.value }));
  };

  async function submit(event) {
    event.preventDefault();
    setLoading(true);
    setError("");
    try {
      const response = await fetch("/api/plan", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          ...form,
          place: form.place.trim() || "a nearby park or trail",
          interests: form.interests.trim() || "nature, fresh air, and a screen-free break",
          duration: Number(form.duration)
        })
      });
      if (!response.ok) throw new Error("The backend could not create a plan.");
      const data = await response.json();
      setPlan(data);
      setPacked({});
      setCompleted({});
      setTimeout(() => document.querySelector("#results")?.scrollIntoView({ behavior: "smooth" }), 60);
    } catch {
      setError("Could not connect to Spring Boot. Make sure the backend is running on port 8080.");
    } finally {
      setLoading(false);
    }
  }

  const missionCount = plan?.missions?.length || 0;
  const completedCount = Object.values(completed).filter(Boolean).length;

  return (
    <>
      <header className="topbar">
        <a className="brand" href="#"><span className="brand-mark"><Leaf size={18} /></span> trailbuddy<span className="brand-ai">.ai</span></a>
        <div className={`status ${health?.modelAvailable ? "local" : "ready"}`}>
          <span className="status-dot" />
          {health?.modelAvailable ? "Local AI ready" : "Offline planner ready"}
        </div>
      </header>

      <main>
        <section className="hero">
          <div className="hero-copy">
            <p className="eyebrow">A LITTLE LESS SCROLLING. A LITTLE MORE SKY.</p>
            <h1>Your next good<br /><em>day is outside.</em></h1>
            <p className="hero-text">A practical outdoor plan made around your time, energy, and curiosity. Make the plan here, then put your phone away.</p>
            <div className="hero-tags"><span>🌿 Nature-first</span><span>🔒 Local AI option</span><span>📵 Screen-light by design</span></div>
          </div>
          <div className="hero-art" aria-label="Stylized mountains, sun and trees">
            <div className="sun" /><div className="mountain mountain-back" /><div className="mountain mountain-front" />
            <div className="hill" /><Trees className="tree tree-one" /><Trees className="tree tree-two" />
            <div className="trail" /><div className="art-label">GO WHERE<br />THE GREEN IS</div>
          </div>
        </section>

        <section className="planner card">
          <div className="section-heading">
            <div><p className="eyebrow">YOUR OUTDOOR RESET</p><h2>Make a plan</h2></div>
            <span className="step-badge">01 / PLAN</span>
          </div>
          <form onSubmit={submit}>
            <div className="field full">
              <label htmlFor="place">Where are you heading?</label>
              <div className="input-wrap"><MapPin size={17} /><input id="place" name="place" value={form.place} onChange={update} maxLength={120} placeholder="e.g. a local park, a garden, a nearby trail" /></div>
              <small>Use a place you know or can verify. TrailBuddy does not check live access or weather.</small>
            </div>

            <div className="field full">
              <label>What sounds good?</label>
              <div className="activity-options">
                {activities.map(({ value, label, icon: Icon }) => (
                  <button type="button" key={value} className={`activity-option ${form.activity === value ? "selected" : ""}`} onClick={() => setForm((old) => ({ ...old, activity: value }))}>
                    <Icon size={19} /><span>{label}</span>
                  </button>
                ))}
              </div>
            </div>

            <div className="field">
              <label htmlFor="duration">How much time do you have?</label>
              <select id="duration" name="duration" value={form.duration} onChange={update}>
                <option value="30">30 minutes</option><option value="60">1 hour</option><option value="120">2 hours</option><option value="180">3 hours</option><option value="240">4 hours</option><option value="360">Half a day</option>
              </select>
            </div>
            <div className="field">
              <label htmlFor="level">Your pace</label>
              <select id="level" name="level" value={form.level} onChange={update}>
                <option value="beginner">Easy-going / beginner</option><option value="moderate">Moderate</option><option value="experienced">Experienced</option>
              </select>
            </div>
            <div className="field full">
              <label htmlFor="interests">What would you like to notice?</label>
              <input id="interests" name="interests" value={form.interests} onChange={update} maxLength={180} placeholder="e.g. birds, fall colors, quiet, wildflowers" />
            </div>
            {error && <p className="error full" role="alert">{error}</p>}
            <button className="primary-btn full" type="submit" disabled={loading}>
              <span>{loading ? "Making your plan…" : "Build my outdoor plan"}</span><ArrowUpRight size={18} />
            </button>
          </form>
          <p className="privacy-note"><span>●</span> When Ollama is available, prompts go to your local model—not a hosted AI API.</p>
        </section>

        {plan && (
          <section id="results" className="results">
            <div className="results-top">
              <div><p className="eyebrow">YOUR FIELD GUIDE</p><h2>{plan.title || "Your outdoor plan"}</h2><p className="muted">{plan.intro}</p></div>
              <button className="secondary-btn" onClick={() => window.print()}><Printer size={16} /> Print / save PDF</button>
            </div>
            <p className="source-note"><Sparkles size={13} /> {plan.source === "ollama" ? "Generated by your local open-weight model via Ollama." : "Created with TrailBuddy's built-in planner. Add Ollama for local open-weight AI."}</p>
            <div className="results-grid">
              <section className="card itinerary-card">
                <div className="card-heading"><span className="round-icon"><Compass size={19} /></span><div><h3>Your route through the day</h3><p>Keep it flexible. Enjoy the moment.</p></div></div>
                <div className="timeline">
                  {(plan.itinerary || []).map((item, i) => <div className="timeline-item" key={`${item.title}-${i}`}>
                    <div className="timeline-time">{item.time}</div><h4>{item.title}</h4><p>{item.detail}</p>
                  </div>)}
                </div>
              </section>

              <section className="card">
                <div className="card-heading"><span className="round-icon"><Backpack size={19} /></span><div><h3>Pack light, pack smart</h3><p>A quick pre-leave checklist</p></div></div>
                <div className="checklist">{(plan.packing || []).map((item, i) => <label className="check-row" key={`${item}-${i}`}>
                  <input type="checkbox" checked={Boolean(packed[i])} onChange={(e) => setPacked((old) => ({ ...old, [i]: e.target.checked }))} /><span>{item}</span>
                </label>)}</div>
              </section>

              <section className="card">
                <div className="card-heading"><span className="round-icon"><Sparkles size={19} /></span><div><h3>Touch-grass missions</h3><p>Try a few. No need to do them all.</p></div></div>
                <div className="mission-list">{(plan.missions || []).map((item, i) => <label className="mission" key={`${item}-${i}`}>
                  <input type="checkbox" checked={Boolean(completed[i])} onChange={(e) => setCompleted((old) => ({ ...old, [i]: e.target.checked }))} /><span>{item}</span>
                </label>)}</div>
                <p className="mission-count">{completedCount} of {missionCount} completed</p>
              </section>

              <section className="card">
                <div className="card-heading"><span className="round-icon"><ShieldCheck size={19} /></span><div><h3>Leave it better</h3><p>Safety and care come first</p></div></div>
                <ul className="safety-list">{(plan.safety || []).map((item, i) => <li key={i}>{item}</li>)}</ul>
              </section>
            </div>
            <div className="offline-banner"><span>📵</span><div><strong>Now close this tab.</strong><p>Print your field guide, check conditions, tell someone if needed, and enjoy being out there.</p></div><Check size={20} /></div>
            <button className="secondary-btn new-plan" onClick={() => { setPlan(null); window.scrollTo({ top: 0, behavior: "smooth" }); }}>← Make another plan</button>
          </section>
        )}

        <section className="how-it-works">
          <p className="eyebrow">OPEN INNOVATION, OUT IN THE OPEN</p>
          <h2>AI that doesn't need to own your data.</h2>
          <div className="value-grid">
            <div><span>01</span><h3>Run it locally</h3><p>Connect Ollama and an open-weight model to generate plans on your own machine.</p></div>
            <div><span>02</span><h3>Works without AI</h3><p>If the model is unavailable, a built-in planner still creates a useful plan—no cloud API key required.</p></div>
            <div><span>03</span><h3>Go offline after planning</h3><p>The experience is designed to help you leave the screen behind, not keep you scrolling.</p></div>
          </div>
        </section>
      </main>
      <footer><span>✳ trailbuddy.ai</span><span>Made for the real world. Touch grass responsibly.</span></footer>
    </>
  );
}
