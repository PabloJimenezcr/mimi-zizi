(() => {
    'use strict';

    const sidebar = document.getElementById('adminSidebar');
    const menuToggle = document.querySelector('[data-admin-menu-toggle]');
    const closeButtons = document.querySelectorAll('[data-admin-menu-close]');
    const backdrop = document.querySelector('.admin-sidebar-backdrop');

    if (!sidebar || !menuToggle || !backdrop) {
        return;
    }

    const isMobile = window.matchMedia('(max-width: 991.98px)');

    const getFocusableElements = () => {
        return Array.from(
            sidebar.querySelectorAll(
                'a[href], button:not([disabled])'
            )
        );
    };

    const openMenu = () => {
        sidebar.classList.add('is-open');
        backdrop.classList.add('is-open');
        document.body.classList.add('admin-menu-open');

        menuToggle.setAttribute('aria-expanded', 'true');

        sidebar.querySelector('[data-admin-menu-first]')?.focus();
    };

    const closeMenu = (restoreFocus = false) => {
        sidebar.classList.remove('is-open');
        backdrop.classList.remove('is-open');
        document.body.classList.remove('admin-menu-open');

        menuToggle.setAttribute('aria-expanded', 'false');

        if (restoreFocus) {
            menuToggle.focus();
        }
    };

    menuToggle.addEventListener('click', () => {
        const isOpen = sidebar.classList.contains('is-open');

        if (isOpen) {
            closeMenu(true);
        } else {
            openMenu();
        }
    });

    closeButtons.forEach((button) => {
        button.addEventListener('click', () => {
            closeMenu(true);
        });
    });

    sidebar.querySelectorAll('a[href]').forEach((link) => {
        link.addEventListener('click', () => {
            if (isMobile.matches) {
                closeMenu();
            }
        });
    });

    document.addEventListener('keydown', (event) => {
        if (!sidebar.classList.contains('is-open')) {
            return;
        }

        if (event.key === 'Escape') {
            closeMenu(true);
            return;
        }

        if (event.key !== 'Tab') {
            return;
        }

        const focusable = getFocusableElements();
        const first = focusable[0];
        const last = focusable[focusable.length - 1];

        if (!first || !last) {
            return;
        }

        if (event.shiftKey && document.activeElement === first) {
            event.preventDefault();
            last.focus();
        } else if (!event.shiftKey && document.activeElement === last) {
            event.preventDefault();
            first.focus();
        }
    });

    window.addEventListener('resize', () => {
        if (!isMobile.matches) {
            closeMenu();
        }
    });

})();