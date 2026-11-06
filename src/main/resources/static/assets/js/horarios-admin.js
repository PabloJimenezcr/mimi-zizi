(() => {
    'use strict';

    const forms = document.querySelectorAll('.schedule-state-form');

    forms.forEach((form) => {

        form.addEventListener('submit', async (event) => {

            event.preventDefault();

            const fecha = form.dataset.scheduleDate || '';
            const horario = form.dataset.scheduleTime || '';
            const action = form.dataset.actionLabel || 'modificar';

            let confirmed = false;

            if (window.Swal) {

                const result = await Swal.fire({

                    title: action === 'bloquear'
                        ? '¿Bloquear este horario?'
                        : '¿Habilitar este horario?',

                    html: `
                        <p>Estás a punto de <strong>${action}</strong> este bloque:</p>
                        <strong>${fecha}</strong>
                        <br>
                        <span>${horario}</span>
                    `,

                    icon: 'question',

                    showCancelButton: true,

                    confirmButtonText: action === 'bloquear'
                        ? 'Sí, bloquear'
                        : 'Sí, habilitar',

                    cancelButtonText: 'Cancelar',

                    reverseButtons: true,

                    focusCancel: true,

                    confirmButtonColor: '#ff4f8b',

                    cancelButtonColor: '#647681'
                });

                confirmed = result.isConfirmed;

            } else {

                confirmed = window.confirm(
                    `¿Deseas ${action} el horario ${fecha}, ${horario}?`
                );

            }

            if (confirmed) {
                form.submit();
            }

        });

    });

})();