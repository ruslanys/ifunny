<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>FunCode Challenge</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bulma@0.8.0/css/bulma.min.css">
    <script defer src="https://use.fontawesome.com/releases/v5.12.0/js/all.js"></script>

    <style>
        .feed-loader {
            width: 100px;
            height: 100px;
            margin: 20px auto auto;
        }

        video {
            width: 100%;
        }

        #backToTop {
            bottom: 30px;
            right: 30px;
            position: fixed;
            z-index: 100000;
            transition: background-color .3s, opacity .5s, visibility .5s;
            opacity: 0;
            visibility: hidden;
        }

        #backToTop.show {
            opacity: 1;
            visibility: visible;
        }
    </style>
</head>
<body>

<button id="backToTop" class="button is-warning" onclick="scrollToTop(1000)"><i class="fas fa-arrow-up"></i></button>

<section class="hero is-warning is-bold">
    <div class="hero-body">
        <div class="container">
            <div class="columns is-vcentered">
                <div class="column is-2">
                    <figure class="image is-square">
                        <img src="/img/logo.png">
                    </figure>
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

<section class="section">
    <div class="container" id="app">

        <div class="columns is-centered">
            <div class="column is-half">

                <div class="columns">
                    <div class="column">
                        <div class="field is-grouped is-grouped-centered">
                            <div class="control has-icons-left">
                                <div class="select">
                                    <select @change="onChange($event)" v-model="params.language">
                                        <option disabled>Language</option>
                                        <option value="de">German</option>
                                        <option value="es" disabled>Spanish</option>
                                        <option value="it" disabled>Italian</option>
                                        <option value="pt" disabled>Portuguese</option>
                                        <option value="ru" disabled>Russian</option>
                                    </select>
                                </div>
                                <div class="icon is-small is-left has-text-info">
                                    <i class="fas fa-language"></i>
                                </div>
                            </div>
                            <div class="control has-icons-left">
                                <div class="select">
                                    <select @change="onChange($event)" v-model="params.sortBy">
                                        <option disabled>Sort By</option>
                                        <option value="publishDateTime">Newest</option>
                                        <option value="likes">Interesting</option>
                                    </select>
                                </div>
                                <div class="icon is-small is-left has-text-info">
                                    <i class="fas fa-sort-amount-down"></i>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>


                <div class="columns is-multiline">

                    <div v-for="meme in feed" class="column is-full">
                        <div class="card">
                            <header class="card-header">
                                <p class="card-header-title">
                                    {{ meme.title }}
                                </p>
                            </header>

                            <div class="card-image">
                                <figure v-if="meme.contentType.startsWith('image')" class="image">
                                    <img :src="meme.url" :alt="meme.title">
                                </figure>
                                <figure v-else class="image">
                                    <video controls muted>
                                        <source :src="meme.url" :type="meme.contentType"/>
                                    </video>
                                </figure>
                            </div>

                            <div v-if="meme.author || meme.publishDateTime" class="card-content">
                                <div class="content">
                                    <p v-if="meme.author" class="subtitle is-6">@{{meme.author}}</p>
                                    <time v-if="meme.publishDateTime">{{meme.publishDateTime | formatDate }}</time>
                                </div>
                            </div>

                            <footer v-if="meme.likes > 0 || meme.comments > 0" class="card-footer">
                                <p v-if="meme.likes" class="card-footer-item">
                                    <span class="icon has-text-danger"><i class="fas fa-heart"></i></span>
                                    {{ meme.likes }} Likes
                                </p>
                                <p v-if="meme.comments" class="card-footer-item">
                                    <span class="icon has-text-info"><i class="fas fa-comment"></i></span>
                                    {{ meme.comments }} Comments
                                </p>
                            </footer>
                        </div>
                    </div>


                    <div v-if="isLoading" class="column is-full">
                        <div class="loader feed-loader"></div>
                    </div>

                    <div v-if="!isLoading && !theEndReached" class="column is-full has-text-centered">
                        <button v-on:click="loadNextPage" class="button is-light is-warning is-medium">Load More...</button>
                    </div>

                </div>

            </div>

        </div>
    </div>
</section>

<script src="https://cdn.jsdelivr.net/npm/vue@2.6.11/dist/vue.min.js" integrity="sha256-ngFW3UnAN0Tnm76mDuu7uUtYEcG3G5H1+zioJw3t+68=" crossorigin="anonymous"></script>
<script src="https://cdn.jsdelivr.net/npm/axios@0.19.0/dist/axios.min.js" integrity="sha256-S1J4GVHHDMiirir9qsXWc8ZWw74PHHafpsHp5PXtjTs=" crossorigin="anonymous"></script>
<script src="https://cdn.jsdelivr.net/npm/moment@2.24.0/min/moment.min.js" integrity="sha256-4iQZ6BVL4qNKlQ27TExEhBN1HFPvAvAMbFavKKosSWQ=" crossorigin="anonymous"></script>

<script>
    const app = new Vue({
        el: '#app',
        data: {
            feed: [],
            isLoading: true,
            theEndReached: false,
            params: {
                offset: 0,
                limit: 10,
                language: 'de',
                sortBy: 'publishDateTime',
            }
        },
        methods: {
            getPage: function () {
                this.isLoading = true;

                axios
                    .get('/api/feed', {
                        params: this.params
                    })
                    .then(response => {
                        this.feed.push(...response.data.list);
                        this.isLoading = false;

                        if (response.data.list.length < this.params.limit) {
                            this.theEndReached = true;
                        }
                    })
            },
            loadNextPage: function () {
                this.params.offset += this.params.limit;
                this.getPage()
            },
            handleScroll: function (event) {
                // Lazy loading
                let bottomOfWindow = document.documentElement.scrollTop + window.innerHeight === document.documentElement.offsetHeight;
                if (bottomOfWindow && !this.isLoading && !this.theEndReached) {
                    this.loadNextPage()
                }

                // Back to top
                let backToTopButton = document.getElementById("backToTop");
                if (window.scrollY > 300) {
                    backToTopButton.classList.add("show");
                } else {
                    backToTopButton.classList.remove("show");
                }
            },
            onChange: function (event) {
                this.feed = [];
                this.params.offset = 0;
                this.theEndReached = false;
                this.getPage();
            }
        },
        created: function () {
            window.addEventListener('scroll', this.handleScroll);
        },
        destroyed() {
            window.removeEventListener('scroll', this.handleScroll);
        },
        mounted: function () {
            this.getPage()
        },
        filters: {
            formatDate: function (value) {
                return moment(value).calendar()
            }
        }
    });

    function scrollToTop(scrollDuration) {
        const scrollStep = -window.scrollY / (scrollDuration / 15);
        const scrollInterval = setInterval(function () {
            if (window.scrollY !== 0) {
                window.scrollBy(0, scrollStep);
            } else clearInterval(scrollInterval);
        }, 15);
    }
</script>
</body>
</html>
