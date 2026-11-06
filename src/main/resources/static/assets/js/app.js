(() => {
  'use strict';

  const $ = (s, ctx = document) => ctx.querySelector(s);
  const $$ = (s, ctx = document) => [...ctx.querySelectorAll(s)];
  const prefersReduced = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

  // Smooth scrolling via Lenis + GSAP ticker.
  let lenis;
  if (window.Lenis && window.gsap && !prefersReduced) {
    lenis = new window.Lenis({ duration: 1.05, smoothWheel: true, touchMultiplier: 1.1 });
    window.mimiLenis = lenis;
    lenis.on('scroll', () => window.ScrollTrigger?.update());
    window.gsap.ticker.add((time) => lenis.raf(time * 1000));
    window.gsap.ticker.lagSmoothing(0);
  }

  // Header state + active anchor.
  const header = $('#siteHeader');
  const navLinks = $$('.nav-link');
  const sections = $$('main section[id]');
  const setHeader = () => header.classList.toggle('scrolled', window.scrollY > 40);
  window.addEventListener('scroll', setHeader, { passive: true }); setHeader();
  const observer = new IntersectionObserver(entries => entries.forEach(entry => {
    if (!entry.isIntersecting) return;
    navLinks.forEach(a => a.classList.toggle('active', a.getAttribute('href') === `#${entry.target.id}`));
  }), { rootMargin: '-30% 0px -55% 0px' });
  sections.forEach(s => observer.observe(s));

  // Mobile menu: abre/cierra el panel en tablet y móvil.
  const navToggle = $('#navToggle');
  const navMenu = $('#navMenu');
  const closeNavMenu = () => {
    if (!navMenu || !navToggle) return;
    navMenu.classList.remove('open');
    navToggle.setAttribute('aria-expanded', 'false');
    navToggle.setAttribute('aria-label', 'Abrir menú');
  };

  navToggle?.addEventListener('click', () => {
    if (!navMenu) return;
    const open = navMenu.classList.toggle('open');
    navToggle.setAttribute('aria-expanded', String(open));
    navToggle.setAttribute('aria-label', open ? 'Cerrar menú' : 'Abrir menú');
  });

  $$('#navMenu a, #navMenu button').forEach((element) => {
    element.addEventListener('click', closeNavMenu);
  });

  document.addEventListener('click', (event) => {
    if (!navMenu || !navToggle || !navMenu.classList.contains('open')) return;
    if (!navMenu.contains(event.target) && !navToggle.contains(event.target)) closeNavMenu();
  });

  window.addEventListener('resize', () => {
    if (window.innerWidth > 1199) closeNavMenu();
  });

  // Bubble canvas: many small grooming bubbles that respond to pointer movement.
  const canvas = $('#bubbleCanvas');
  const ctx = canvas?.getContext('2d');
  const bubbles = [];
  const pointer = { x: -1000, y: -1000, active: false };
  const resizeCanvas = () => { canvas.width = innerWidth * devicePixelRatio; canvas.height = innerHeight * devicePixelRatio; canvas.style.width = innerWidth + 'px'; canvas.style.height = innerHeight + 'px'; ctx.setTransform(devicePixelRatio, 0, 0, devicePixelRatio, 0, 0); };
  const makeBubble = () => ({ x: Math.random() * innerWidth, y: innerHeight + Math.random() * 180, r: Math.random() * 18 + 7, v: .25 + Math.random() * .65, drift: (Math.random() - .5) * .5, alpha: .12 + Math.random() * .25 });
  const initBubbles = () => { for (let i = 0; i < 34; i++) bubbles.push({ ...makeBubble(), y: Math.random() * innerHeight }); };
  const drawBubbles = () => {
    if (!ctx) return;
    ctx.clearRect(0, 0, innerWidth, innerHeight);
    bubbles.forEach(b => {
      b.y -= b.v; b.x += b.drift;
      if (b.y < -50) Object.assign(b, makeBubble());
      if (pointer.active) {
        const dx = b.x - pointer.x, dy = b.y - pointer.y, d = Math.hypot(dx, dy); if (d < 130 && d > 0) { b.x += dx / d * 1.3; b.y += dy / d * 1.3; }
      }
      const g = ctx.createRadialGradient(b.x - b.r * .3, b.y - b.r * .3, 1, b.x, b.y, b.r); g.addColorStop(0, 'rgba(255,255,255,.45)'); g.addColorStop(.28, 'rgba(255,255,255,.12)'); g.addColorStop(.75, 'rgba(143,231,232,.08)'); g.addColorStop(1, 'rgba(255,255,255,0)');
      ctx.beginPath(); ctx.fillStyle = g; ctx.arc(b.x, b.y, b.r, 0, Math.PI * 2); ctx.fill(); ctx.strokeStyle = `rgba(255,255,255,${b.alpha})`; ctx.lineWidth = 1; ctx.stroke();
    });
    requestAnimationFrame(drawBubbles);
  };
  if (canvas && !prefersReduced) { resizeCanvas(); initBubbles(); drawBubbles(); window.addEventListener('resize', resizeCanvas); }
  window.addEventListener('pointermove', e => { pointer.x = e.clientX; pointer.y = e.clientY; pointer.active = true; });

  // Custom cursor: solo en dispositivos con mouse; se inicializa en la posición real.
  const dot = $('#cursorDot');
  const ring = $('#cursorRing');
  const hasFinePointer = window.matchMedia('(hover: hover) and (pointer: fine)').matches;

  if (hasFinePointer && dot && ring) {
    let mouseX = 0;
    let mouseY = 0;
    let ringX = 0;
    let ringY = 0;
    let hasPointerPosition = false;

    window.addEventListener('pointermove', (event) => {
      if (event.pointerType === 'touch') return;
      if (event.target.closest('.login-modal')) {
        dot.style.opacity = '0';
        ring.style.opacity = '0';
        return;
      }
      mouseX = event.clientX;
      mouseY = event.clientY;
      if (!hasPointerPosition) {
        ringX = mouseX;
        ringY = mouseY;
        hasPointerPosition = true;
      }
      dot.style.opacity = '1';
      ring.style.opacity = '1';
    }, { passive: true });

    const followCursor = () => {
      if (hasPointerPosition) {
        ringX += (mouseX - ringX) * .16;
        ringY += (mouseY - ringY) * .16;
        dot.style.left = `${mouseX}px`;
        dot.style.top = `${mouseY}px`;
        ring.style.left = `${ringX}px`;
        ring.style.top = `${ringY}px`;
      }
      window.requestAnimationFrame(followCursor);
    };
    followCursor();

    $$('a, button, .service-card, .story-stage, .hero__logo-card').forEach((element) => {
      element.addEventListener('mouseenter', () => ring.classList.add('hover'));
      element.addEventListener('mouseleave', () => ring.classList.remove('hover'));
    });
  }

  // GSAP reveal + parallax.
  if (window.gsap && window.ScrollTrigger && !prefersReduced) {
    gsap.registerPlugin(ScrollTrigger);
    gsap.from('.hero__copy > *', { y: 34, opacity: 0, duration: 1.05, stagger: .12, ease: 'power3.out', delay: .9 });
    gsap.from('.hero__visual > *', { y: 45, opacity: 0, scale: .96, duration: 1.15, stagger: .09, ease: 'power3.out', delay: 1.05 });
    gsap.utils.toArray('.display-title').forEach(el => gsap.from(el, { y: 55, opacity: 0, duration: 1, ease: 'power3.out', scrollTrigger: { trigger: el, start: 'top 82%' } }));
    gsap.utils.toArray('.reveal-card').forEach((el, i) => gsap.from(el, { y: 70, opacity: 0, scale: .98, duration: .8, delay: i * .06, ease: 'power3.out', scrollTrigger: { trigger: el, start: 'top 88%' } }));
    gsap.from('.process-step', { y: 60, opacity: 0, stagger: .12, duration: .75, ease: 'power3.out', scrollTrigger: { trigger: '.process-track', start: 'top 78%' } });
    gsap.to('.process-line span', { width: '100%', duration: 1.4, ease: 'power2.out', scrollTrigger: { trigger: '.process-track', start: 'top 70%', end: 'top 30%', scrub: true } });
    $$('.hero__visual [data-parallax]').forEach(el => { const p = parseFloat(el.dataset.parallax); gsap.to(el, { yPercent: -30 * p, xPercent: 12 * p, scrollTrigger: { trigger: '.hero', start: 'top top', end: 'bottom top', scrub: true } }); });
    gsap.to('.hero__circle', { rotation: 360, duration: 36, repeat: -1, ease: 'none' });
    gsap.to('.phone-device', { y: 12, rotation: 1.5, duration: 2.8, repeat: -1, yoyo: true, ease: 'sine.inOut' });
    gsap.from('.phone-stage__word', { opacity: 0, scale: .92, stagger: .08, duration: 1.1, scrollTrigger: { trigger: '.phone-stage', start: 'top 78%' } });
    gsap.from('.phone-copy > *', { y: 28, opacity: 0, stagger: .1, duration: .65, ease: 'power3.out', scrollTrigger: { trigger: '.phone-copy', start: 'top 84%' } });
    gsap.from('.phone-float', { y: 28, opacity: 0, stagger: .12, duration: .7, ease: 'power3.out', scrollTrigger: { trigger: '.phone-stage', start: 'top 72%' } });
  }

  // Simple ritual tabs.
  const rituals = [
    { word: 'FRESH', text: 'Baño suave, secado cuidadoso y la sensación de salir como nuevo.', image: 'https://images.unsplash.com/photo-1596492784531-6e6ebac7e15b?auto=format&fit=crop&w=1200&q=85' },
    { word: 'STYLE', text: 'Corte y estilizado pensado para su raza, pelaje y personalidad.', image: 'https://images.unsplash.com/photo-1601758064130-2ea6f2f6fc3d?auto=format&fit=crop&w=1200&q=85' },
    { word: 'GLOW', text: 'Detalles premium: uñas, oídos, deslanado y ese toque final.', image: 'https://images.unsplash.com/photo-1534361960057-19889db9621e?auto=format&fit=crop&w=1200&q=85' },
    { word: 'HAPPY', text: 'Porque el objetivo final no es que se vea increíble: es que se vaya feliz.', image: 'https://images.unsplash.com/photo-1558788353-f76d92427f16?auto=format&fit=crop&w=1200&q=85' }
  ];
  const ritualWord = $('#ritualWord'), ritualText = $('#ritualText'), ritualImage = $('#ritualImage');
  $$('.ritual-dot').forEach((btn, i) => btn.addEventListener('click', () => {
    $$('.ritual-dot').forEach(b => b.classList.remove('is-active')); btn.classList.add('is-active');
    const r = rituals[i];
    if (window.gsap && !prefersReduced) { gsap.to([ritualWord, ritualText, ritualImage], { opacity: 0, y: 10, duration: .22, onComplete: () => { ritualWord.textContent = r.word; ritualText.textContent = r.text; ritualImage.style.backgroundImage = `url("${r.image}")`; gsap.to([ritualWord, ritualText, ritualImage], { opacity: 1, y: 0, duration: .45, ease: 'power3.out' }) } }); } else { ritualWord.textContent = r.word; ritualText.textContent = r.text; ritualImage.style.backgroundImage = `url("${r.image}")`; }
  }));

  // Grooming finder.
  const answers = {}; const finderSteps = $$('.finder-step'); const finderResult = $('#finderResult'); const resultTitle = $('#resultTitle'); const resultText = $('#resultText');
  $$('.finder-options button').forEach(btn => btn.addEventListener('click', () => {
    const step = btn.closest('.finder-step'); const q = Number(step.dataset.q); answers[q] = btn.dataset.value;
    step.querySelectorAll('button').forEach(b => b.classList.remove('is-chosen')); btn.classList.add('is-chosen');
    if (q < 2) { finderSteps.forEach((s, i) => s.classList.toggle('is-active', i === q + 1)); }
    if (Object.keys(answers).length === 3) {
      const ritual = answers[2] === 'spa' || answers[0] === 'chill' ? 'Zen Spa' : answers[2] === 'style' || answers[0] === 'royal' ? 'Signature Style' : 'Fresh & Fluffy';
      const desc = ritual === 'Zen Spa' ? 'Una pausa de bienestar, mimos y detalles suaves para salir relajado.' : ritual === 'Signature Style' ? 'Grooming completo y un look WOW diseñado alrededor de su personalidad.' : 'Baño, secado y cuidado esencial para una sensación de frescura total.';
      resultTitle.textContent = ritual; resultText.textContent = desc; finderResult.hidden = false;
      if (window.gsap && !prefersReduced) gsap.fromTo(finderResult, { y: 25, opacity: 0 }, { y: 0, opacity: 1, duration: .65, ease: 'power3.out' });
    }
  }));

  // La lógica del modal de acceso se mantiene independiente en auth.js.

  // Magnetic buttons.
  if (!prefersReduced) {
    $$('.btn-pill,.btn-hero,.service-card__cta').forEach(btn => btn.addEventListener('pointermove', e => { const r = btn.getBoundingClientRect(); const x = (e.clientX - r.left - r.width / 2) * .16; const y = (e.clientY - r.top - r.height / 2) * .16; btn.style.transform = `translate(${x}px,${y}px)`; }));
    $$('.btn-pill,.btn-hero,.service-card__cta').forEach(btn => btn.addEventListener('pointerleave', () => btn.style.transform = ''));
  }

  // Pause motion control.
  const motionToggle = $('#motionToggle');
  motionToggle?.addEventListener('click', () => { const paused = document.body.classList.toggle('motion-paused'); motionToggle.setAttribute('aria-pressed', paused ? 'true' : 'false'); motionToggle.innerHTML = paused ? '<i class="ri-play-line"></i> Reanudar movimiento' : '<i class="ri-pause-line"></i> Pausar movimiento'; if (window.gsap) gsap.globalTimeline.paused(paused); });

})();
