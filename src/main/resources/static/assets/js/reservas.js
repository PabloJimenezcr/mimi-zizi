
document.addEventListener('DOMContentLoaded', () => {

    const form = document.getElementById('reservation-form');

    if (!form) {
        return;
    }

    const petInputs = form.querySelectorAll(
        'input[name="mascotaId"]'
    );

    const serviceInputs = form.querySelectorAll(
        '.js-reservation-service'
    );

    const summaryPet = document.getElementById('summary-pet');
    const summaryServices = document.getElementById('summary-services');
    const summaryCount = document.getElementById('summary-service-count');
    const summaryDuration = document.getElementById('summary-duration');
    const summaryTotal = document.getElementById('summary-total');
    const feedback = document.getElementById('reservation-feedback');
    const submitButton = form.querySelector('.reservation-submit');

    const currencyFormatter = new Intl.NumberFormat('es-CR', {
        style: 'currency',
        currency: 'CRC',
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    });

    function actualizarMascota() {

        const selectedPet = form.querySelector(
            'input[name="mascotaId"]:checked'
        );

        const summaryPetImage = document.getElementById(
            'summary-pet-image'
        );

        const summaryPetFallback = document.getElementById(
            'summary-pet-fallback'
        );

        if (!selectedPet) {

            summaryPet.textContent = 'Selecciona una mascota';

            summaryPetImage.hidden = true;
            summaryPetImage.removeAttribute('src');

            summaryPetFallback.hidden = false;

            return;
        }

        const petName = selectedPet.dataset.petName;
        const petPhoto = selectedPet.dataset.petPhoto;
        const hasPhoto = selectedPet.dataset.petHasPhoto === 'true';

        // Actualizar nombre
        summaryPet.textContent = petName;

        // Si tiene fotografía, mostrarla
        if (hasPhoto && petPhoto) {

            summaryPetImage.alt = `Fotografía de ${petName}`;

            summaryPetImage.onerror = () => {
                summaryPetImage.hidden = true;
                summaryPetImage.removeAttribute('src');
                summaryPetFallback.hidden = false;
            };

            summaryPetImage.src = petPhoto;

            summaryPetImage.hidden = false;
            summaryPetFallback.hidden = true;

        } else {

            // Si no tiene fotografía, mostrar icono
            summaryPetImage.hidden = true;
            summaryPetImage.removeAttribute('src');

            summaryPetFallback.hidden = false;
        }
    }

    function actualizarServicios() {

        const selectedServices = Array.from(serviceInputs)
            .filter(input => input.checked);

        let total = 0;
        let duration = 0;

        summaryServices.replaceChildren();

        if (selectedServices.length === 0) {

            const emptyItem = document.createElement('li');

            emptyItem.className =
                'reservation-summary__empty-service';

            emptyItem.textContent =
                'Aún no has seleccionado servicios.';

            summaryServices.appendChild(emptyItem);

        } else {

            selectedServices.forEach(input => {

                const serviceName = input.dataset.serviceName;
                const price = Number(input.dataset.price);
                const serviceDuration = Number(input.dataset.duration);

                total += Number.isFinite(price) ? price : 0;
                duration += Number.isFinite(serviceDuration)
                    ? serviceDuration
                    : 0;

                const item = document.createElement('li');

                const name = document.createElement('span');
                name.textContent = serviceName;

                const amount = document.createElement('strong');
                amount.textContent = currencyFormatter.format(price);

                item.append(name, amount);

                summaryServices.appendChild(item);
            });
        }

        summaryCount.textContent = selectedServices.length;

        summaryDuration.textContent =
            `${duration} min`;

        summaryTotal.textContent =
            currencyFormatter.format(total);
    }

    petInputs.forEach(input => {
        input.addEventListener('change', actualizarMascota);
    });

    serviceInputs.forEach(input => {
        input.addEventListener('change', actualizarServicios);
    });

    form.addEventListener('submit', event => {

        const selectedPet = form.querySelector(
            'input[name="mascotaId"]:checked'
        );

        const selectedServices = form.querySelectorAll(
            '.js-reservation-service:checked'
        );

        if (!selectedPet) {

            event.preventDefault();

            feedback.textContent =
                'Selecciona la mascota para la que deseas reservar.';

            const firstPet = petInputs[0];

            if (firstPet) {
                firstPet.focus();
            }

            return;
        }

        if (selectedServices.length === 0) {

            event.preventDefault();

            feedback.textContent =
                'Selecciona al menos un servicio para continuar.';

            if (serviceInputs.length > 0) {
                serviceInputs[0].focus();
            }

            return;
        }

        feedback.textContent =
            'Estamos registrando tu reserva. Por favor, no cierres esta página.';

        if (submitButton) {

            submitButton.disabled = true;

            submitButton.innerHTML = `
                <span>Registrando tu cita...</span>
                <i class="ri-loader-4-line" aria-hidden="true"></i>
            `;
        }
    });

    actualizarMascota();
    actualizarServicios();

});
