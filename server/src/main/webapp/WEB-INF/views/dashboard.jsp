<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.Collections" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="uy.edu.isbo.control.model.Computer" %>
<%@ page import="uy.edu.isbo.control.model.Laboratory" %>
<%@ page import="uy.edu.isbo.control.model.agent.PendingAgent" %>
<%@ page import="uy.edu.isbo.control.model.User" %>
<%@ page import="uy.edu.isbo.control.web.support.Html" %>
<%@ page import="uy.edu.isbo.control.web.view.AgentInstallation" %>
<%
    List<Laboratory> labs = (List<Laboratory>) request.getAttribute("laboratories");
    List<Computer> computers = (List<Computer>) request.getAttribute("computers");
    List<User> users = (List<User>) request.getAttribute("users");
    List<PendingAgent> pending = (List<PendingAgent>) request.getAttribute("pendingAgents");

    Map<Long, Boolean> states = (Map<Long, Boolean>) request.getAttribute("states");
    if (states == null) {
        states = Collections.emptyMap();
    }

    Map<Long, String> laboratoryNames = (Map<Long, String>) request.getAttribute("laboratoryNames");
    Map<Long, Long> computerCounts = (Map<Long, Long>) request.getAttribute("computerCounts");
    long onlineCount = (Long) request.getAttribute("onlineCount");

    String activePage = (String) request.getAttribute("activePage");
    String pageTitle = (String) request.getAttribute("pageTitle");
    String pageDescription = (String) request.getAttribute("pageDescription");
    String csrf = Html.escape(request.getAttribute("csrf"));
%>
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
        <title><%= pageTitle %> · Control ISBO</title>
        <link
            rel="stylesheet"
            href="${pageContext.request.contextPath}/assets/css/base.css"
        />
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/tables.css" />
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/login.css" />
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/responsive.css" />
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/components.css" />
        <script
            src="${pageContext.request.contextPath}/assets/js/dashboard.js?v=20261102-7"
            defer
        ></script>
    </head>
    <body>
        <a class="skip-link" href="#page-content">Ir al contenido</a>
        <div class="app-layout">
            <aside class="sidebar" aria-label="Navegación principal">
                <div class="brand">
                    <img
                        class="brand-mark"
                        src="${pageContext.request.contextPath}/assets/images/favicon.jpg"
                        alt="ISBO · Instituto Superior Brazo Oriental"
                        width="52"
                        height="52"
                    />
                    <span>
                        ISBO
                        <small>Control de laboratorios</small>
                    </span>
                </div>
                <div class="side-label">GESTIÓN DE EQUIPOS</div>
                <nav class="side-nav">
                    <a
                        href="${pageContext.request.contextPath}/app?page=overview"
                        <%= "overview".equals(activePage) ? "aria-current=\"page\"" : "" %>
                    >
                        Resumen
                    </a>
                    <a href="${pageContext.request.contextPath}/app?page=labs" <%= "labs".equals(activePage) ? "aria-current=\"page\"" : "" %>>
                        Laboratorios
                    </a>
                    <a href="${pageContext.request.contextPath}/app?page=pcs" <%= "pcs".equals(activePage) ? "aria-current=\"page\"" : "" %>>
                        Computadoras
                    </a>
                    <a
                        href="${pageContext.request.contextPath}/app?page=pending"
                        <%= "pending".equals(activePage) ? "aria-current=\"page\"" : "" %>
                    >
                        Vincular equipos
                        <% if (!pending.isEmpty()) { %>
                        <b class="nav-count"><%= pending.size() %></b>
                        <% } %>
                    </a>
                    <a href="${pageContext.request.contextPath}/app?page=users" <%= "users".equals(activePage) ? "aria-current=\"page\"" : "" %>>
                        Usuarios
                    </a>
                </nav>
                <div class="sidebar-bottom">
                    <span class="sidebar-dot"></span>
                    ISBO · Área 433
                    <br />
                    <small>Programación Avanzada · 2026</small>
                </div>
            </aside>
            <main class="workspace">
                <header class="topbar">
                    <span>
                        Panel de administración
                        <span class="topbar-separator">/</span>
                        <strong><%= pageTitle %></strong>
                    </span>
                    <form method="post">
                        <input type="hidden" name="csrf" value="<%= csrf %>" />
                        <input type="hidden" name="action" value="logout" />
                        <button class="secondary logout">Cerrar sesión</button>
                    </form>
                </header>
                <div class="content">
                    <section id="page-content" class="intro">
                        <h1><%= pageTitle %></h1>
                        <p><%= pageDescription %></p>
                    </section>
                    <% if (request.getAttribute("message") != null) { %>
                    <p class="alert" role="status"><%= Html.escape(request.getAttribute("message")) %></p>
                    <% } %>

                    <% if ("overview".equals(activePage)) { %>
                    <%@ include file="pages/overview.jspf" %>
                    <% } else if ("labs".equals(activePage)) { %>
                    <%@ include file="pages/labs.jspf" %>
                    <% } else if ("pcs".equals(activePage)) { %>
                    <%@ include file="pages/pcs.jspf" %>
                    <% } else if ("pending".equals(activePage)) { %>
                    <%@ include file="pages/pending.jspf" %>
                    <% } else if ("users".equals(activePage)) { %>
                    <%@ include file="pages/users.jspf" %>
                    <% } %>

                    <footer>ISBO · Proyecto de Programación Avanzada · 2026</footer>
                </div>
            </main>
        </div>
    </body>
</html>
