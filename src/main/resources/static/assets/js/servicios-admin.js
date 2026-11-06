(() => {
    'use strict';

    const forms = document.querySelectorAll('.service-state-form');

    forms.forEach((form) => {

        form.addEventListener('submit', async (event) => {

            event.preventDefault();

            const serviceName = form.dataset.serviceName || 'este servicio';
            const action = form.dataset.actionLabel || 'modificar';

            let confirmed = false;

            if (window.Swal) {

                const result = await Swal.fire({
                    title: action === 'desactivar'
                        ? '¿Desactivar este servicio?'
                        : '¿Activar este servicio?',

                    html: `Estás a punto de <strong>${action}</strong>:<br>${serviceName}`,

                    icon: 'question',

                    showCancelButton: true,

                    confirmButtonText: action === 'desactivar'
                        ? 'Sí, desactivar'
                        : 'Sí, activar',

                    cancelButtonText: 'Cancelar',

                    reverseButtons: true,

                    focusCancel: true,

                    confirmButtonColor: '#ff4f8b',

                    cancelButtonColor: '#647681'
                });

                confirmed = result.isConfirmed;

            } else {

                confirmed = window.confirm(
                    `¿Deseas ${action} el servicio "${serviceName}"?`
                );

            }

            if (confirmed) {
                form.submit();
            }

        });

    });

})();