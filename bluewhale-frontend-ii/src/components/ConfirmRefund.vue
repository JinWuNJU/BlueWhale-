<script lang="ts">

import {orderRefund} from "../api/order.ts";

export default {
  data() {
    return {
      value: ''//退款原因暂时后端不留存
    }
  },
  methods: {
    handleConfirm() {
      orderRefund({
        order_id:this.order_id
      }).then(() => {
        ElMessage.success("确认退款成功！")
        location.reload()
      })
    }
  },
  props: [
    "order_id"
  ]
}

</script>

<template>
  <el-dialog
      width="500"
      destroy-on-close
      draggable
      center
      @closed="value=''"
  >
    <div style="display: flex; justify-content: center; align-items: center">
      <el-select
          v-model="value"
          placeholder="请选择退款原因"
          style="width: 240px"
      >
        <el-option value="商品描述不符" label="商品描述不符"/>
        <el-option value="商品破损" label="商品破损"/>
        <el-option value="假货" label="假货"/>
        <el-option value="商品错发" label="商品错发"/>
        <el-option value="不想要了" label="不想要了"/>
        <el-option value="其他原因" label="其他原因"/>
      </el-select>
    </div>
    <el-result
        icon="warning"
        title="确认退款"
        style="padding: 0;margin-top: 40px"
        sub-title="您确认退款了吗？（确认后无法撤销）"
    />

    <template #footer>
      <div class="dialog-footer" v-if="value">
        <el-button type="primary" @click="handleConfirm">确认</el-button>
      </div>
    </template>
  </el-dialog>
</template>