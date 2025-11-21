import router from './views/router/index.js'
import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import App from './App.vue'
import '@/assets/global.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'

import axios from 'axios'
import VueAxios from "vue-axios";

const app = createApp(App)

app.use(router)

app.use(ElementPlus,{
    locale:zhCn,
})

axios.defaults.baseURL = "http://localhost:8080"
axios.defaults.withCredentials = true
app.use(VueAxios,axios);

app.mount('#app')
