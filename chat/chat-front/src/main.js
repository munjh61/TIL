import { createApp } from 'vue'
import App from './App.vue'
import vuetify from './plugins/vuetify'
import router from '@/router/index.js'
import axios from 'axios'

const app = createApp(App)

axios.interceptors.request.use(
    config => {
        const token = localStorage.getItem('token')
        if(token){
            config.headers['Authorization'] = `Bearer ${token}`
        }
        return config
    },
    error => {
        return Promise.reject(error)
    }
)

app.use(router)
app.use(vuetify)
app.mount('#app')
