(() => {
  'use strict';

  const $ = (selector, context = document) => context.querySelector(selector);
  const $$ = (selector, context = document) => [...context.querySelectorAll(selector)];
  const modal = $('#loginModal');
  const card = $('#authCard');
  const scrollArea = $('#authScrollArea');
  const loginView = $('#loginView');
  const registerView = $('#registerView');
  if (!modal || !card || !scrollArea || !loginView || !registerView) return;

  let previousFocus = null;
  let savedScrollY = 0;
  let modalOpen = false;

  const setStatus = (element, message = '', type = '') => {
    if (!element) return;
    element.textContent = message;
    element.className = `form-status${type ? ` ${type}` : ''}`;
  };

  const showLoginSuccess = (message) => {
    let notice = $('#loginSuccessNotice', loginView);

    if (!notice) {
      notice = document.createElement('div');
      notice.id = 'loginSuccessNotice';
      notice.className = 'auth-notice auth-notice--success';
      notice.setAttribute('role', 'status');

      const description = $('#loginTitle', loginView)?.nextElementSibling;

      if (description) {
        description.insertAdjacentElement('afterend', notice);
      } else {
        loginView.prepend(notice);
      }
    }

    notice.textContent = message;
    notice.hidden = false;
  };

  const changeView = (view) => {
    const isRegister = view === 'register';
    loginView.hidden = isRegister;
    registerView.hidden = !isRegister;
    modal.setAttribute('aria-labelledby', isRegister ? 'registerTitle' : 'loginTitle');
    scrollArea.scrollTop = 0;
    setStatus($('#formStatus'));
    setStatus($('#registerStatus'));
  };

  const lockPageScroll = () => {
    if (modalOpen) return;
    savedScrollY = window.scrollY || window.pageYOffset;
    document.documentElement.classList.add('auth-is-open');
    document.body.classList.add('auth-is-open');
    document.body.style.position = 'fixed';
    document.body.style.top = `-${savedScrollY}px`;
    document.body.style.left = '0';
    document.body.style.right = '0';
    document.body.style.width = '100%';
    window.mimiLenis?.stop();
    modalOpen = true;
  };

  const unlockPageScroll = () => {
    if (!modalOpen) return;
    document.documentElement.classList.remove('auth-is-open');
    document.body.classList.remove('auth-is-open');
    document.body.style.position = '';
    document.body.style.top = '';
    document.body.style.left = '';
    document.body.style.right = '';
    document.body.style.width = '';
    window.scrollTo(0, savedScrollY);
    window.mimiLenis?.start();
    modalOpen = false;
  };

  const openModal = (view = 'login') => {
    previousFocus = document.activeElement;
    changeView(view);
    modal.hidden = false;
    modal.setAttribute('aria-hidden', 'false');
    lockPageScroll();
    window.setTimeout(() => {
      const first = view === 'register' ? $('#registerName') : $('#loginEmail');
      first?.focus({ preventScroll: true });
    }, 50);
  };

  const closeModal = () => {
    modal.hidden = true;
    modal.setAttribute('aria-hidden', 'true');
    unlockPageScroll();
    previousFocus?.focus({ preventScroll: true });
  };

  $$('.js-login, .js-book').forEach((button) => button.addEventListener('click', () => openModal('login')));
  $$('.js-register').forEach((button) => button.addEventListener('click', () => openModal('register')));
  $('#showRegister')?.addEventListener('click', () => changeView('register'));
  $('#showLogin')?.addEventListener('click', () => changeView('login'));
  $$('.js-close-auth').forEach((element) => element.addEventListener('click', closeModal));

  document.addEventListener('keydown', (event) => {
    if (modal.hidden) return;
    if (event.key === 'Escape') {
      event.preventDefault();
      closeModal();
      return;
    }
    if (event.key !== 'Tab') return;
    const focusable = $$('button:not([disabled]), input:not([disabled]), a[href], [tabindex]:not([tabindex="-1"])', card)
      .filter((element) => element.offsetParent !== null);
    if (!focusable.length) return;
    const first = focusable[0];
    const last = focusable[focusable.length - 1];
    if (event.shiftKey && document.activeElement === first) {
      event.preventDefault();
      last.focus();
    } else if (!event.shiftKey && document.activeElement === last) {
      event.preventDefault();
      first.focus();
    }
  });

  const connectPasswordToggle = (buttonId, inputId) => {
    const button = $(buttonId);
    const input = $(inputId);
    button?.addEventListener('click', () => {
      if (!input) return;
      const show = input.type === 'password';
      input.type = show ? 'text' : 'password';
      button.innerHTML = show
        ? '<i class="ri-eye-off-line" aria-hidden="true"></i>'
        : '<i class="ri-eye-line" aria-hidden="true"></i>';
      button.setAttribute('aria-label', show ? 'Ocultar contraseña' : 'Mostrar contraseña');
    });
  };
  connectPasswordToggle('#togglePass', '#loginPass');
  connectPasswordToggle('#toggleRegisterPass', '#registerPass');

  const setFieldError = (input, message) => {
    if (!input) return;
    const error = input.closest('.auth-field')?.querySelector('[data-field-error]');
    input.classList.toggle('is-invalid', Boolean(message));
    input.setAttribute('aria-invalid', message ? 'true' : 'false');
    if (error) {
      error.textContent = message || '';
      error.classList.toggle('is-visible', Boolean(message));
    }
  };

  const clearFieldOnInput = (form) => {
    if (!form) return;
    $$('input', form).forEach((input) => {
      const clear = () => setFieldError(input, '');
      input.addEventListener('input', clear);
      input.addEventListener('change', clear);
    });
  };

  const isEmail = (value) => /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/.test(value.trim());
  const loginForm = $('#loginForm');
  const registerForm = $('#registerForm');
  clearFieldOnInput(loginForm);
  clearFieldOnInput(registerForm);

  loginForm?.addEventListener('submit', (event) => {
    const email = $('#loginEmail');
    const password = $('#loginPass');
    const emailError = !email.value.trim()
      ? 'Escribe tu correo electrónico.'
      : (!isEmail(email.value) ? 'Ingresa un correo electrónico válido.' : '');
    const passwordError = !password.value ? 'Escribe tu contraseña.' : '';
    setFieldError(email, emailError);
    setFieldError(password, passwordError);
    if (emailError || passwordError) {
      event.preventDefault();
      setStatus($('#formStatus'), 'Revisa los campos marcados para continuar.', 'error');
      (emailError ? email : password).focus({ preventScroll: false });
      return;
    }
    const submit = $('#loginSubmit');
    if (submit) {
      submit.disabled = true;
      submit.querySelector('span').textContent = 'Verificando…';
      submit.classList.add('is-loading');
    }
  });


  registerForm?.addEventListener('submit', async (event) => {
    const name = $('#registerName');
    const phone = $('#registerPhone');
    const email = $('#registerEmail');
    const password = $('#registerPass');
    const confirmation = $('#registerConfirmPass');
    const terms = $('#registerTerms');

    const nameValue = name.value.trim();
    const digits = phone.value.replace(/\D/g, '');

    const errors = {
      nombre: !nameValue ? 'Escribe tu nombre completo.'
        : (nameValue.length < 3 ? 'El nombre debe tener al menos 3 caracteres.'
          : (!/^[A-Za-zÁÉÍÓÚÜÑáéíóúüñ\s'.-]+$/.test(nameValue)
            ? 'Utiliza letras y espacios para tu nombre.' : '')),

      telefono: !phone.value.trim() ? 'Escribe tu número de teléfono.'
        : (digits.length < 8 || digits.length > 15
          ? 'Ingresa un teléfono válido de 8 a 15 dígitos.' : ''),

      correo: !email.value.trim() ? 'Escribe tu correo electrónico.'
        : (!isEmail(email.value) ? 'Ingresa un correo electrónico válido.' : ''),

      password: !password.value ? 'Crea una contraseña.'
        : (password.value.length < 8
          ? 'La contraseña debe tener al menos 8 caracteres.' : ''),

      confirmacion: !confirmation.value ? 'Confirma tu contraseña.'
        : (confirmation.value !== password.value
          ? 'Las contraseñas no coinciden.' : ''),

      terminos: !terms.checked
        ? 'Debes aceptar los términos para crear tu cuenta.' : ''
    };

    setFieldError(name, errors.nombre);
    setFieldError(phone, errors.telefono);
    setFieldError(email, errors.correo);
    setFieldError(password, errors.password);
    setFieldError(confirmation, errors.confirmacion);
    setFieldError(terms, errors.terminos);

    const firstInvalid = [name, phone, email, password, confirmation, terms]
      .find((input) => input.classList.contains('is-invalid'));

    if (firstInvalid) {
      event.preventDefault();

      setStatus(
        $('#registerStatus'),
        'Revisa los campos marcados para completar tu registro.',
        'error'
      );

      firstInvalid.focus({ preventScroll: false });
      return;
    }

    // Evita la recarga tradicional de la página.
    event.preventDefault();

    const submit = $('#registerSubmit');
    const registeredEmail = email.value.trim();

    if (submit) {
      submit.disabled = true;
      submit.querySelector('span').textContent = 'Creando tu cuenta…';
      submit.classList.add('is-loading');
    }

    try {
      const response = await fetch(registerForm.action, {
        method: 'POST',
        body: new FormData(registerForm),
        headers: {
          'X-Requested-With': 'XMLHttpRequest',
          'Accept': 'application/json'
        },
        credentials: 'same-origin'
      });

      const data = await response.json();

      if (!response.ok || !data.success) {

        // Muestra los errores de validación enviados por Spring.
        Object.entries(data.errors || {}).forEach(([field, message]) => {
          const input = registerForm.elements.namedItem(field);

          if (input instanceof HTMLElement) {
            setFieldError(input, message);
          }
        });

        setStatus(
          $('#registerStatus'),
          data.message || 'Revisa los datos e inténtalo nuevamente.',
          'error'
        );

        registerForm.querySelector('.is-invalid')?.focus({
          preventScroll: false
        });

        return;
      }

      // Registro exitoso.
      registerForm.reset();

      // Conservamos el correo para facilitar el inicio de sesión.
      $('#loginEmail').value = registeredEmail;

      // Cambiamos al modal de inicio de sesión sin recargar.
      changeView('login');

      // Mostramos el mensaje de confirmación.
      showLoginSuccess(
        data.message || '¡Tu cuenta Mimi y Zizi está lista! Ahora puedes iniciar sesión.'
      );

      $('#loginEmail')?.focus({ preventScroll: true });

    } catch (error) {
      console.error('Error al registrar usuario:', error);

      setStatus(
        $('#registerStatus'),
        'No pudimos completar tu registro. Inténtalo nuevamente.',
        'error'
      );

    } finally {
      if (submit) {
        submit.disabled = false;
        submit.querySelector('span').textContent = 'Crear mi cuenta';
        submit.classList.remove('is-loading');
      }
    }
  });



  // Recupera la vista y los mensajes del servidor después de una validación o un redirect.
  const page = document.body;
  const requestedView = page.dataset.authView;
  const notice = page.dataset.authNotice;
  const loginError = page.dataset.loginError === 'true';
  if (requestedView === 'register' || requestedView === 'login' || notice || loginError) {
    openModal(requestedView === 'register' ? 'register' : 'login');
  }
  if (loginError) {
    setStatus($('#formStatus'), 'El correo o la contraseña no son correctos. Comprueba tus datos e inténtalo de nuevo.', 'error');
  }
})();
