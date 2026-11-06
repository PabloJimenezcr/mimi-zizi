
(() => {
    'use strict';

    const loader = document.getElementById('siteLoader');

    if (!loader) {
        return;
    }

    const root = document.documentElement;
    const body = document.body;

    const prefersReducedMotion = window.matchMedia(
        '(prefers-reduced-motion: reduce)'
    ).matches;

    const startTime = performance.now();

    let finished = false;
    let readyTimer = null;
    let fallbackTimer = null;

    // Bloquear el desplazamiento mientras está visible.
    root.classList.add('site-loader-active');
    body.classList.add('site-loader-active');

    function hideLoader() {

        if (finished) {
            return;
        }

        finished = true;

        if (readyTimer !== null) {
            window.clearTimeout(readyTimer);
        }

        if (fallbackTimer !== null) {
            window.clearTimeout(fallbackTimer);
        }

        // Comenzar la transición de salida.
        loader.setAttribute('aria-hidden', 'true');
        loader.classList.add('site-loader--leaving');

        // Liberar la página inmediatamente.
        root.classList.remove('site-loader-active');
        body.classList.remove('site-loader-active');

        // Retirar el componente después de la transición.
        window.setTimeout(() => {
            loader.remove();
        }, prefersReducedMotion ? 0 : 500);
    }

    function finishWhenReady() {

        if (finished || readyTimer !== null) {
            return;
        }

        // Duración visual mínima para evitar que aparezca
        // y desaparezca de forma brusca en cargas rápidas.
        const minimumDuration = prefersReducedMotion ? 0 : 650;

        const elapsed = performance.now() - startTime;

        const remaining = Math.max(
            0,
            minimumDuration - elapsed
        );

        readyTimer = window.setTimeout(hideLoader, remaining);
    }

    // Esperar a que el HTML esté construido.
    if (document.readyState === 'loading') {

        document.addEventListener(
            'DOMContentLoaded',
            finishWhenReady,
            { once: true }
        );

    } else {

        finishWhenReady();
    }

    // Recuperación cuando el navegador vuelve desde otra página.
    window.addEventListener('pageshow', (event) => {

        if (event.persisted) {
            hideLoader();
        }

    }, { once: true });

    // Seguridad: nunca bloquear indefinidamente al cliente.
    fallbackTimer = window.setTimeout(
        hideLoader,
        3500
    );

})();
