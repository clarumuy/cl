(() => {
    function openEditor(section, title, field) {
        const editor = document.getElementById(`${section}-editor`);
        editor.querySelector("h2").textContent = title;
        editor.showModal();
        document.body.classList.add("modal-open");
        document.getElementById(field).focus();
    }

    function configureIp(value = "") {
        const input = document.getElementById("pc-ip");
        const automatic = value.startsWith("agent:");
        input.value = automatic ? "" : value;
        input.required = !automatic;
        input.disabled = automatic;
    }

    function createRecord({ section, title, field }) {
        const form = document.querySelector(`#${section}-editor form`);
        form.reset();
        form.elements.id.value = "0";
        if (section === "pcs") configureIp();
        if (section === "users") document.getElementById("user-password").required = true;
        openEditor(section, title, field);
    }

    function editLab({ id, name, room }) {
        document.getElementById("lab-id").value = id;
        document.getElementById("lab-name").value = name;
        document.getElementById("lab-room").value = room;
        openEditor("labs", "Editar laboratorio", "lab-name");
    }

    function editPc({ id, name, ip, laboratory }) {
        document.getElementById("pc-id").value = id;
        document.getElementById("pc-name").value = name;
        document.getElementById("pc-lab").value = laboratory;
        configureIp(ip);
        openEditor("pcs", "Editar computadora", "pc-name");
    }

    function editUser({ id, name }) {
        document.getElementById("user-id").value = id;
        document.getElementById("user-name").value = name;
        const password = document.getElementById("user-password");
        password.value = "";
        password.required = false;
        openEditor("users", "Editar usuario", "user-name");
    }

    async function copyInstallation(button) {
        const text = document.getElementById(button.dataset.target);
        try {
            await navigator.clipboard.writeText(text.value);
            button.textContent = "Copiado";
        } catch {
            text.focus();
            text.select();
            button.textContent = "Seleccionado: usá Ctrl+C";
        }
    }

    const commands = {
        create: (button) => createRecord(button.dataset),
        "edit-lab": (button) => editLab(button.dataset),
        "edit-pc": (button) => editPc(button.dataset),
        "edit-user": (button) => editUser(button.dataset),
        close: (button) => button.closest("dialog").close(),
        copy: copyInstallation,
    };

    document.addEventListener("click", (event) => {
        if (!(event.target instanceof Element)) return;
        const button = event.target.closest("button[data-command]");
        if (button) commands[button.dataset.command]?.(button);
    });

    document.addEventListener("submit", (event) => {
        const message = event.target.dataset.confirm;
        if (message && !window.confirm(message)) event.preventDefault();
    });

    function synchronizeModals() {
        document.body.classList.toggle(
            "modal-open",
            Boolean(document.querySelector(".record-modal[open]")),
        );
    }

    document.querySelectorAll(".record-modal").forEach((dialog) => {
        dialog.addEventListener("close", synchronizeModals);
        dialog.addEventListener("toggle", synchronizeModals);
    });

    const search = document.getElementById("computer-search");
    const rows = [...document.querySelectorAll("#pcs tbody tr")].filter(
        (row) => !row.querySelector(".table-empty"),
    );
    search?.addEventListener("input", () => {
        const query = search.value.trim().toLocaleLowerCase("es");
        let visible = 0;
        rows.forEach((row) => {
            row.hidden = ![...row.cells]
                .slice(0, 4)
                .some((cell) => cell.textContent.toLocaleLowerCase("es").includes(query));
            if (!row.hidden) visible++;
        });
        document.getElementById("search-count").textContent = query
            ? `${visible} de ${rows.length} equipos`
            : "";
    });
})();
