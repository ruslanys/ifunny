<!DOCTYPE html>
<html>
<@layout.head>
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
</@layout.head>

<@layout.body>
    <button id="backToTop" class="button is-warning" onclick="scrollToTop(1000)"><i class="fas fa-arrow-up"></i></button>

    <section class="section">
        <div class="container" id="app">
            <div class="columns is-centered">
                <div class="column is-half">

                    <!-- Controls -->
                    <div class="columns">
                        <div class="column">
                            <div class="field is-grouped is-grouped-centered">

                                <!-- Language selection -->
                                <div class="control has-icons-left">
                                    <div class="select">
                                        <select @change="onChange($event)" v-model="params.language">
                                            <option disabled>Language</option>
                                            <option value="de">German</option>
                                            <option value="fr">French</option>
                                            <option value="it">Italian</option>
                                            <option value="es" disabled>Spanish</option>
                                            <option value="pt" disabled>Portuguese</option>
                                            <option value="ru" disabled>Russian</option>
                                        </select>
                                    </div>
                                    <div class="icon is-small is-left has-text-info">
                                        <i class="fas fa-language"></i>
                                    </div>
                                </div>

                                <!-- Sorting type selection -->
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

                    <!-- Feed -->
                    <div class="columns is-multiline">

                        <div v-for="meme in feed" class="column is-full">
                            <div class="card">
                                <header class="card-header">
                                    <p class="card-header-title">
                                        {{ meme.title }}
                                    </p>
                                    <a v-bind:href="'/meme/' + meme.id" class="card-header-icon" aria-label="Share" target="_blank">
                                      <span class="icon">
                                        <i class="fas fa-share-alt"></i>
                                      </span>
                                    </a>
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
                            <button v-on:click="loadNextPage" class="button is-light is-warning is-medium">Load
                                More...
                            </button>
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
                loadPage: function () {
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
                    this.loadPage()
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

                    localStorage.setItem("language", this.params.language);
                    localStorage.setItem("sortBy", this.params.sortBy);

                    this.loadPage();
                }
            },
            created: function () {
                window.addEventListener('scroll', this.handleScroll);
            },
            destroyed() {
                window.removeEventListener('scroll', this.handleScroll);
            },
            mounted: function () {
                let storageLanguage = localStorage.getItem("language");
                let storageSortBy = localStorage.getItem("sortBy");

                if (storageLanguage) {
                    this.params.language = storageLanguage;
                }
                if (storageSortBy) {
                    this.params.sortBy = storageSortBy;
                }

                this.loadPage()
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
</@layout.body>
</html>
