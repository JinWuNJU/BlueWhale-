<script setup lang="ts">

</script>


<template>
	<body>
		<el-main class="main-frame">
			<el-result icon="error" style="margin: 0;"/>
			<h1 style="color: #646464; font-size: 48px">当前页面不存在或您无权访问 :(</h1>
			<router-link to="/home">
				<el-button type="primary">回到主页</el-button>
			</router-link>
		</el-main>
	</body>
</template>


<style scoped>
body {

}

.main-frame {
  width: 100%;
  height: 100%;

  display: flex;
  align-items: center;
  justify-content: center;
	flex-direction: column;

	background-image: url("../assets/img/background/cart.jpg");
	background-size: cover;
	overflow: hidden;
}
</style>
