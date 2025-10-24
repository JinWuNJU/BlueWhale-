<script setup lang="ts">
import {markRaw, onMounted, ref} from "vue";
import {fullScreenLoadingShort} from "../../utils/visuals.ts";
import {Bell, ShoppingCart, ChatDotSquare, User,} from '@element-plus/icons-vue'
import Header from "../../components/Header.vue";
import {
	createSessionsInfo,
	deleteSessionsInfo,
	fetchMessagesInfo,
	fetchSessionsInfo,
	sendMessageInfo
} from "../../api/chatbox.ts";
import {getUserById} from "../../api/resolve_id.ts";

onMounted(() => {
	fullScreenLoadingShort();
	getSessions();
});

const sessions = ref([]);
const messages = ref([]);
const currentMsg = ref("");
const centerDialogVisible = ref(false);

const viewingItem = ref(1);
const viewingSession = ref(0);

const role = ref(sessionStorage.role);
const userId = ref(sessionStorage.userId);

const switchViewingItem = (item) => {
	viewingItem.value = item;
  viewingSession.value = 0;
	getSessions();
	//getMessages();
}

const switchViewingSession = (session) => {
	viewingSession.value = session;
	getMessages();
}

const getSessions = () => {
	switch ( viewingItem.value ) {
		case 1:
			// TODO - We may add system alerts (or may not)
			break;
		case 2:
			fetchSessionsInfo({
				staffId: role.value == 'STAFF' ? parseInt(userId.value) : -1,
				customerId: role.value == 'CUSTOMER' ? parseInt(userId.value) : -1,
 			}).then(res => {
				sessions.value = res.data.result;
				for ( let i = 0; i < sessions.value.length; i++ ) {
					getUserById({
						userId: sessions.value[i].staffId
					}).then(res => {
						sessions.value[i].staffDetail = res.data.result;
					})
					getUserById({
						userId: sessions.value[i].customerId
					}).then(res => {
						sessions.value[i].customerDetail = res.data.result;
					})
				}
				console.log(sessions.value);
			})
			break;
		case 3:
			// TODO - We may add comment alert part (or may not)
			break;
		default:
			break;
	}
}

const confirmClear = () => {
	deleteSessionsInfo({
		staffId: role.value == 'STAFF' ? parseInt(userId.value) : -1,
		customerId: role.value == 'CUSTOMER' ? parseInt(userId.value) : -1,
	}).then(() => {
		ElMessage.success("已清空所有会话！");
	})
}

const getMessages = () => {
	fetchMessagesInfo({
		chatBoxId: sessions.value[viewingSession.value - 1].chatBoxId,
	}).then(res => {
		messages.value = res.data.result;
	})
}

const sendMessage = () => {
	sendMessageInfo({
		cid: sessions.value[viewingSession.value - 1].chatBoxId,
		msg: currentMsg.value,
		sender: role.value == 'CUSTOMER',
		/* 0 = STAFF, 1 = CUSTOMER */
	}).then(() => {
		// ElMessage.success("发送消息成功！");
    currentMsg.value = '';
		getMessages();
	}).catch(() => {
		ElMessage.error("发送消息失败！");
	})
}

setInterval(function (){if(viewingSession.value != 0){getMessages()}}, 2.5 * 1000);

</script>

<template>
	<body>
	<Header/>
	<el-main class="main">
		<div style="display: flex; flex-direction: row">
			<h1 style="font-size: 88px; margin: 20px 0 0 100px; color: #282828">我的消息</h1>
			<router-link to="/home" v-slot="{navigate}">
				<el-button
					@click="navigate"
					style="margin: 80px 0 0 20px; font-size: 24px; width: 135px; height: 50px">
					返回主页
				</el-button>
			</router-link>
		</div>
		<div style="display: flex; flex-direction: row; align-content: center">
			<el-row style="margin: 20px 0 0 100px; padding: 0; width: 300px">
				<el-col style="width: 200px">
					<el-menu default-active="1">
						<el-menu-item index="1" @click="switchViewingItem(1)">
							<el-icon><Bell /></el-icon>
							<span>系统消息</span>
						</el-menu-item>
						<el-menu-item index="2" @click="switchViewingItem(2)">
							<el-icon><ShoppingCart /></el-icon>
							<span>客服消息</span>
						</el-menu-item>
						<el-menu-item disabled index="3" @click="switchViewingItem(3)">
							<el-icon><ChatDotSquare /></el-icon>
							<span>评论/回复消息（未开放）</span>
						</el-menu-item>
					</el-menu>
				</el-col>
			</el-row>

			<el-card
				class="whole-card"
				v-if="viewingItem == 1"
				style="width: 1100px; height: 600px; border: 5px solid rgb(196, 196, 196); margin: 12px"
			>
				<el-timeline style="max-width: 1000px">
					<el-timeline-item timestamp="2024/5/25" placement="top">
						<el-card>
							<h2>BlueWhale 更新日志</h2>
							<p>加入了联系客服、创建会话的功能。</p>
						</el-card>
					</el-timeline-item>
				</el-timeline>
			</el-card>

<!--			TODO - STAFF SELECTING-->
			<el-card
				class="whole-card"
				v-if="viewingItem == 2"
				style="width: 300px; height: 600px; border: 5px solid rgb(196, 196, 196); margin: 12px"
			>
				<div style="display: flex; flex-direction: column; align-content: center">
					<h1 style="margin: 0 0 10px 0">消息列表</h1>
					<el-scrollbar
						style="height: 410px; margin-bottom: 20px"
					>
						<el-card
							class="contact-card"
							v-for="i in sessions.length"
							@click="switchViewingSession(i)"
							style="height: 90px; margin-bottom: 10px"
						>
							<div
								v-if="sessions[i - 1].staffDetail != undefined
									&& sessions[i - 1].customerDetail != undefined"
							     style="display: flex; flex-direction: row; "
							>
								<el-badge is-dot class="item" :offset="[-10, 10]">
									<el-avatar
										v-if="role == 'CUSTOMER'"
										:src="sessions[i - 1].staffDetail.avatarUrl"
										style="width: 60px; height: 60px; margin-right: 10px"
									/>
									<el-avatar
										v-if="role == 'STAFF'"
										:src="sessions[i - 1].customerDetail.avatarUrl"
										style="width: 60px; height: 60px; margin-right: 10px"
									/>
								</el-badge>
								<div>
									<h3 style="margin: 5px 0 0 0;">
										{{ role == 'CUSTOMER'
											? sessions[i - 1].staffDetail.name
											: sessions[i - 1].customerDetail.name }}
									</h3>
									<span v-if="role == 'CUSTOMER'" style="margin: 0; color: gray">🏠 -
										{{ sessions[i - 1].staffDetail.shopName }}
									</span>
									<span v-if="role == 'STAFF'" style="margin: 0; color: gray">📞 -
										{{ sessions[i - 1].customerDetail.phone }}
									</span>
								</div>

							</div>
						</el-card>
					</el-scrollbar>
					<el-divider style="margin: 5px 0 15px 0; padding: 0"/>
					<el-card
						class="item-card"
						style="height: 60px; display: flex; flex-direction: row; align-content: center"
						@click="centerDialogVisible = true"
					>
						<span style="font-size: 26px; font-weight: bold;">❗ </span>
						<span style="font-size: 24px; font-family: 幼圆, serif">清空所有会话...</span>
					</el-card>
				</div>
			</el-card>

<!--			TODO - STAFF MESSAGING-->
			<el-card
				class="whole-card"
				v-if="viewingItem == 2 && viewingSession != 0"
				style="
					width: 766px;
					height: 600px;
					background-image: linear-gradient(to top left, #ffffff 0%, #ffffff 100%);
					border: 5px solid rgb(196, 196, 196);
					margin: 12px"
			>
				<div
					v-if="sessions[viewingSession - 1].staffDetail != undefined
						&& sessions[viewingSession - 1].customerDetail != undefined"
				>
					<h3 style="text-align: center; margin: 0">
						{{ role == 'CUSTOMER'
							? sessions[viewingSession - 1].staffDetail.name
							: sessions[viewingSession - 1].customerDetail.name }}
					</h3>
				</div>
				<el-divider style="margin: 10px 0 10px 0">
					<el-icon><User /></el-icon>
				</el-divider>

				<div
					v-if="sessions[viewingSession - 1].staffDetail != undefined
						&& sessions[viewingSession - 1].customerDetail != undefined"
					style="height: 475px">
          <el-scrollbar>
					<div
						v-for="i in messages.length"
					>
<!--						TODO - Code Duplication-->
						<div
							v-if="messages[i - 1] != undefined && (role == 'CUSTOMER' ? 1 : 0) != messages[i - 1].sender"
							style="display: flex; flex-direction: row; align-content: center; margin: 10px 0 0 0"
						>
							<el-avatar
								v-if="messages[i - 1].sender
									&& sessions[viewingSession - 1] != undefined"
								:src="sessions[viewingSession - 1].customerDetail.avatarUrl"
								style="width: 50px; height: 50px; margin-right: 10px"
							/>
							<el-avatar
								v-else
								:src="sessions[viewingSession - 1].staffDetail.avatarUrl"
								style="width: 50px; height: 50px; margin-right: 10px"
							/>
							<div style="display: flex; flex-direction: column; align-content: center; ">
								<el-card class="contact-card" style="width: 200px; height: 40px;">
									<span style="font-family: 幼圆 ,serif; margin: 0; padding: 0">{{ messages[i - 1].msg }}</span>
								</el-card>
								<span style="font-size: 10px">
									{{ messages[i - 1].send_time.substring(0, 10) }} {{messages[i - 1].send_time.substring(11, 19)}}
								</span>
							</div>

						</div>
						<div v-if="messages[i - 1] != undefined && (role == 'CUSTOMER' ? 1 : 0) == messages[i - 1].sender"
						     style="display: flex; flex-direction: row-reverse; align-content: center; margin: 10px 0 0 0">
							<el-avatar
								v-if="messages[i - 1].sender"
								:src="sessions[viewingSession - 1].customerDetail.avatarUrl"
								style="width: 50px; height: 50px; margin-left: 10px"
							/>
							<el-avatar
								v-else
								:src="sessions[viewingSession - 1].staffDetail.avatarUrl"
								style="width: 50px; height: 50px; margin-left: 10px"
							/>
							<div style="display: flex; flex-direction: column; align-content: center; ">
								<el-card class="contact-card" style="width: 200px; height: 40px;">
									<span style="font-family: 幼圆 ,serif; margin: 0; padding: 0">{{ messages[i - 1].msg }}</span>
								</el-card>
								<span style="font-size: 10px">
									{{ messages[i - 1].send_time.substring(0, 10) }} {{messages[i - 1].send_time.substring(11, 19)}}
								</span>
							</div>
						</div>
					</div>
          </el-scrollbar>
				</div>

				<div style="display: flex; justify-content: space-between;">
					<el-input
						clearable
						type="text"
						v-model="currentMsg"
						placeholder="输入你的消息..."
						style="height: 45px; font-size: 18px"
					/>
					<el-button
						type="success"
						@click="sendMessage"
						:disabled="currentMsg == ''"
						style="width: 80px; height: 45px; margin-left: 20px; font-size: 18px"
					>发送</el-button>
				</div>
			</el-card>
		</div>
	</el-main>
	</body>
	<el-dialog
		v-model="centerDialogVisible"
		width="500"
		destroy-on-close
		draggable
		center
	>
		<el-result
			icon="warning"
			title="清空会话"
			style="padding: 0"
			sub-title="确认清空所有会话吗？（该操作无法撤销）"
		/>
		<template #footer>
			<div class="dialog-footer">
				<el-button type="primary" @click="confirmClear">确认</el-button>
			</div>
		</template>
	</el-dialog>
</template>

<style scoped>
body {
	background-image: url("../../assets/img/background/message.jpg");
	background-size: cover;
}
.item-card {
	border: 2px solid rgb(196, 196, 196);
	transition: transform 100ms ease-in-out;
}
.item-card:hover {
	transform: scale(1.04);
}
.item-card >>>.el-card__body {
	padding: 10px;
}
.contact-card {
	border: 2px solid rgb(209, 211, 211);
	transition: transform 100ms ease-in-out;
}
.contact-card:hover {
	transform: scale(1.01919810);
}
.contact-card >>>.el-card__body {
	padding: 10px;
}
</style>