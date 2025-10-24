<script setup lang="ts">
import {onMounted} from "vue";
import {fullScreenLoadingShort} from "../utils/visuals.ts";

onMounted(async () => {
	fullScreenLoadingShort();
})
</script>

<template>
	<body>
		<header class="header">
			<nav class="nav">
				<img
					src="../assets/img/logo.png"
					alt=""
					class="nav__logo"
					id="logo"
				/>
				<ul class="nav__links">
					<li class="nav__item">
						<a class="nav__link" href="#/about">关于</a>
					</li>
					<li class="nav__item">
						<a class="nav__link" href="#/register">注册</a>
					</li>
					<li class="nav__item">
						<a class="nav__link" href="#/login">登录</a>
					</li>
				</ul>
			</nav>

			<div class="header__title">
				<h1 style="margin-left: 50px; font-family: 幼圆, serif; font-weight: bold">
					这里是蓝鲸商城🐋
				</h1>
				<h4 style="margin-left: 50px; font-family: 'Lucida Handwriting',serif">
					A simpler shopping experience for a simpler life.
				</h4>
				<h4 style="margin-left: 50px; color: #6c6c6c; font-family: 'Lucida Handwriting',serif">
					Now Join Us!
				</h4>
				<img
					src="../assets/img/hero.png"
					class="header__img"
					alt=""
				/>
			</div>
		</header>
	</body>
</template>

<style scoped>
* {
	margin: 0;
	padding: 0;
	box-sizing: inherit;
}

body {
	font-family: 'Poppins', sans-serif;
	font-weight: 300;
	color: #444;
	line-height: 1.9;
	background-image: linear-gradient(to top left, #31bdc2 0%, #c4edf3 100%);
}

.nav {
	display: flex;
	justify-content: space-between;
	align-items: center;
	height: 9rem;
	width: 100%;
	padding: 0 6rem 0 0;
	z-index: 100;
}

.nav__logo {
	height: 4.5rem;
	transition: all 0.3s;
}

.nav__links {
	display: flex;
	align-items: center;
	list-style: none;
}

.nav__item {
	margin-left: 4rem;
}

.nav__link:link,
.nav__link:visited {
	font-size: 1.7rem;
	font-weight: 400;
	color: inherit;
	text-decoration: none;
	display: block;
	transition: all 0.3s;
}

.header {
	padding: 0 3rem;
	height: 100vh;
	display: flex;
	flex-direction: column;
	align-items: center;
}

.header__title {
	flex: 1;
	max-width: 115rem;
	display: grid;
	grid-template-columns: 3fr 2fr;
	row-gap: 3rem;
	align-content: center;
	justify-content: center;

}

h1 {
	font-size: 5.5rem;
	line-height: 1.35;
}

h4 {
	font-size: 2.4rem;
	font-weight: 500;
}

.header__img {
	width: 100%;
	grid-column: 2 / 3;
	grid-row: 1 / span 4;
	transform: translateY(-6rem);
}

</style>