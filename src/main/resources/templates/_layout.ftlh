<#assign SITE_NAME = "FunCode Challenge" />

<#macro head title='', image=''>
    <#assign full_title = title?has_content?then("${title} - ${SITE_NAME}", SITE_NAME) />
    <head>
        <!-- Required meta tags -->
        <meta charset="utf-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">

        <!-- OG Tags -->
        <@og full_title image/>

        <!-- Theme Color -->
        <meta name="theme-color" content="#ffcb46">

        <!-- Styles -->
        <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bulma@0.8.0/css/bulma.min.css">
        <script defer src="https://use.fontawesome.com/releases/v5.12.0/js/all.js"></script>

        <!-- // -->
        <title>${full_title}</title>

        <!-- Custom styles for this template -->
        <#nested />
    </head>
</#macro>

<#macro og title='', image=''>
    <meta property="og:type" content="website">
    <meta property="twitter:card" content="summary_large_image">
    <#if title?has_content>
        <meta property="og:title" content="${title}">
        <meta property="twitter:title" content="${title}">
    </#if>
    <#if image?has_content>
        <meta property="og:image" content="${image}">
        <meta property="twitter:image" content="${image}">
    </#if>
</#macro>

<#macro body>
    <body>

    <!-- Header -->
    <section class="hero is-warning is-bold">
        <div class="hero-body">
            <div class="container">
                <div class="columns is-vcentered">
                    <div class="column is-2">
                        <a href="/"><figure class="image is-square"><img src="/img/logo.png" alt="FunCode"></figure></a>
                    </div>
                    <div class="column">
                        <h1 class="title is-1">FunCode</h1>
                        <h2 class="subtitle">Java / Kotlin Challenge</h2>
                        <p>Made with <span class="icon has-text-danger"><i class="fas fa-heart"></i></span> and focus on <span class="icon has-text-success"><i class="fas fa-medal"></i></span></p>
                        <p>Ruslan Molchanov</p>
                    </div>
                </div>
            </div>
        </div>
    </section>

    <!-- Content -->
    <#nested />

    </body>
</#macro>
