import {createWebHistory, createRouter} from 'vue-router'

import Welcome from "@/page/Welcome.vue";
import Login from "@/page/Login.vue";
import Register from "@/page/Register.vue";
import UserProfile from "@/page/UserProfile.vue";
import UserInfoNavbar from "@/components/UserInfoNavbar.vue";
import PostNavbar from "@/components/PostNavbar.vue";
import NewPost from "@/page/NewPost.vue";
import UserHome from "@/page/UserHome.vue";
import HotPost from "@/page/HotPost.vue";
import MinePost from "@/page/MinePost.vue";
import Feedback from "@/page/Feedback.vue";
import MyFollowing from "@/page/MyFollowing.vue";
import FollowingPost from "@/page/FollowingPost.vue";
import MyFans from "@/page/MyFans.vue";
import MyVP from "@/page/MyVP.vue";

const routes = [
    {path: '/', redirect: '/home'},
    {path: '/home', component: Welcome},
    {path: '/login', component: Login},
    {path: '/register', component: Register},
    {path: '/feedback', component: Feedback},

    {
        path: '/user',
        component: UserInfoNavbar,
        children: [
            {
                path: '',
                redirect: '/user/profile'
            },
            {
                path: 'profile',
                component: UserProfile
            },
            {
                path: 'following',
                component: MyFollowing
            },
            {
                path: 'fans',
                component: MyFans
            },
            {
                path: 'vp',
                component: MyVP
            },
            {
                path: 'home/:userId',
                component: UserHome
            }
        ]
    },

    {
        path: '/post',
        component: PostNavbar,
        children: [
            {
                path: '',
                redirect: '/post/new'
            },
            {
                path: 'new',
                component: NewPost
            },
            {
                path: 'hot',
                component: HotPost
            },
            {
                path: 'follow',
                component: FollowingPost
            },
            {
                path: 'mine',
                component: MinePost
            },
        ]
    },
]

const router = createRouter({
    history: createWebHistory(),
    routes,
})

export default router