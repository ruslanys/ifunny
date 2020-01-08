<#assign SITE_NAME = "FunCode Challenge" />

<#macro head title=''>
    <#assign full_title = title?has_content?then("${title} - ${SITE_NAME}", SITE_NAME) />
    <head>
        <!-- Required meta tags -->
        <meta charset="utf-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">

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

<#macro body>
    <body>

    <!-- Header -->
    <section class="hero is-warning is-bold">
        <div class="hero-body">
            <div class="container">
                <div class="columns is-vcentered">
                    <div class="column is-2">
                        <a href="/"><figure class="image is-square"><img src="/img/logo.png"></figure></a>
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
