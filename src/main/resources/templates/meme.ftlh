<!DOCTYPE html>
<html>
<@layout.head meme.title>
    <style>
        video {
            width: 100%;
        }
    </style>
</@layout.head>

<@layout.body>
    <section class="section">
        <div class="container" id="app">
            <div class="columns is-centered">
                <div class="column is-half">

                    <!-- Feed -->
                    <div class="columns is-multiline">

                        <div class="column is-full">
                            <div class="card">
                                <header class="card-header">
                                    <p class="card-header-title">${meme.title}</p>
                                </header>

                                <div class="card-image">
                                    <#if meme.contentType?starts_with("image")>
                                        <figure class="image">
                                            <img src="${meme.url}" alt="${meme.title}">
                                        </figure>
                                    <#else>
                                        <figure class="image">
                                            <video controls muted>
                                                <source src="${meme.url}" type="${meme.contentType}"/>
                                            </video>
                                        </figure>
                                    </#if>
                                </div>

                                <#if meme.author?? || meme.publishDateTime??>
                                    <div class="card-content">
                                        <div class="content">
                                            <#if meme.author??><p class="subtitle is-6">@${meme.author}</p></#if>
                                            <#if meme.publishDateTime??>${meme.publishDateTime}</#if>
                                        </div>
                                    </div>
                                </#if>

                                <#if (meme.likes?? && meme.likes != 0) || (meme.comments?? && meme.comments != 0)>
                                    <footer class="card-footer">
                                        <#if meme.likes?? && meme.likes != 0>
                                            <p class="card-footer-item">
                                                <span class="icon has-text-danger"><i class="fas fa-heart"></i></span>
                                                ${meme.likes} Likes
                                            </p>
                                        </#if>
                                        <#if meme.comments?? && meme.comments != 0>
                                            <p class="card-footer-item">
                                                <span class="icon has-text-info"><i class="fas fa-comment"></i></span>
                                                ${meme.comments} Comments
                                            </p>
                                        </#if>
                                    </footer>
                                </#if>
                            </div>
                        </div>

                        <div class="column is-full has-text-centered">
                            <a href="/" class="button is-light is-warning is-medium">
                                Back to Feed
                            </a>
                        </div>
                    </div>

                </div>

            </div>
        </div>
    </section>
</@layout.body>
</html>
