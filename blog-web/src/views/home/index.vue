<template>
  <div class="home" v-loading="loading">

    <div class="content-layout">
      <main class="home-main-content">
        <Carousel v-if="carouselSlides?.length > 0"
          :slides="carouselSlides" 
          @click="goToPost"
        />
        <MomentsList />
        <ArticleList
          :articles="articleList"
          :loading="loading"
          :total="total"
          :params="params"
          @article-click="goToPost"
          @page-change="changePage"
          class="article-list"
        />
      </main>
      <Sidebar />
    </div>
  </div>
</template>

<script>
import ArticleList from '@/components/ArticleList/index.vue'
import Carousel from '@/views/home/components/carousel.vue'
import Sidebar from '@/components/Sidebar/index.vue'
import MomentsList from '@/views/home/components/moments.vue'
import { getArticlesApi,getCarouselArticlesApi } from '@/api/article'

export default {
  name: 'Home',
  components: {
    ArticleList,
    Carousel,
    Sidebar,
    MomentsList,
  },
  data() {
    return {
      total: 0,
      params: {
        pageNum: 1,
        pageSize: 10,
      },
      articleList: [],
      carouselSlides: [],
      loading: false
    }
  },
  methods: {
    /**
     * 跳转到文章详情
     * @param {number} id 文章id
     */
    goToPost(id) {
      this.$router.push(`/post/${id}`)
    },
    /**
     * 切换页码
     * @param {number} page 页码
     */
    changePage(page) {
      this.params.pageNum = page
      this.getArticleList()
      window.scrollTo({
        top: this.$refs.postsSection?.offsetTop - 80,
        behavior: 'smooth'
      })
    },
    /**
     * 获取文章列表
     */
    getArticleList() {
      this.loading = true
      getArticlesApi(this.params).then(res => {
        if (res.data && res.data.records) {
          this.articleList = res.data.records
          this.total = res.data.total
        }
      }).catch(error => {
        console.error('Failed to fetch articles:', error)
      }).finally(() => {
        this.loading = false
      })
    },
    /**
     * 获取轮播和推荐文章
     */
    getCarouselArticles() {
      getCarouselArticlesApi().then(res => {
        this.carouselSlides = res.data
      })
    }
  },
  created() {
    this.getArticleList()
    this.getCarouselArticles()
  },

}
</script>

<style lang="scss" scoped>
.home {
  max-width: 1400px;
  margin: 0 auto;
  width: 100%;
  padding: $spacing-lg;

  @include responsive(lg) {
    padding: $spacing-sm;
  }

}

.content-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: $spacing-lg * 2;
  padding: 0 $spacing-xl;
  margin-bottom: $spacing-xl * 2;
  min-height: calc(100vh - 80px);
  align-items: start;

  @include responsive(lg) {
    grid-template-columns: 1fr;
    padding: $spacing-sm;
  }
}

.home-main-content {
  min-width: 0;
  width: 100%;
  height: 100%;

  .carousel {
    margin-bottom: $spacing-xl;
    width: 100%;
    max-height: 480px;

    @include responsive(md) {
      margin-bottom: $spacing-xl;
      max-height: 280px;
      :deep(h3) {
        font-size: 1.2em;
      }
    }
  }
}

</style> 
