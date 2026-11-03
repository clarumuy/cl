<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!doctype html>
<html lang="es">
    <head>
        <meta charset="UTF-8" />
        <link
            rel="icon"
            type="image/jpeg"
            href="${pageContext.request.contextPath}/assets/images/favicon.jpg"
        />
        <meta name="viewport" content="width=device-width, initial-scale=1" />
        <title>Acceso | Control ISBO</title>
        <link
            rel="stylesheet"
            href="${pageContext.request.contextPath}/assets/css/base.css"
        />
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/tables.css" />
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/login.css" />
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/responsive.css" />
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/components.css" />
    </head>
    <body class="login-page">
        <div class="login-aside">
            <img
                class="login-aside-mark"
                src="${pageContext.request.contextPath}/assets/images/favicon.jpg"
                alt="ISBO · Instituto Superior Brazo Oriental"
                width="112"
                height="112"
            />
            <div>
                <span>ISBO / ÁREA 433</span>
                <h2>Control de laboratorios.</h2>
                <p>Equipos, salas y acceso remoto para los laboratorios de ISBO.</p>
            </div>
            <small>PROGRAMACIÓN AVANZADA · 2026</small>
        </div>
        <main class="login">
            <h1>Ingresar al sistema</h1>
            <p>Ingresá tus credenciales para continuar.</p>

            <% if ("locked".equals(request.getParameter("error"))) { %>
            <p class="alert" role="alert">
                Demasiados intentos fallidos. Esperá 1 minuto antes de reintentar.
            </p>
            <% } else if (request.getParameter("error") != null) { %>
            <p class="alert" role="alert">Usuario o contraseña incorrectos.</p>
            <% } %>

            <form method="post">
                <input type="hidden" name="action" value="login" />
                <input type="hidden" name="csrf" value="<%= uy.edu.isbo.control.web.support.Html.escape(request.getAttribute("csrf")) %>" />
                <label>
                    Usuario
                    <input name="username" autocomplete="username" required autofocus />
                </label>
                <label>
                    Contraseña
                    <input
                        type="password"
                        name="password"
                        autocomplete="current-password"
                        required
                    />
                </label>
                <button>Ingresar</button>
            </form>
        </main>
    </body>
</html>
