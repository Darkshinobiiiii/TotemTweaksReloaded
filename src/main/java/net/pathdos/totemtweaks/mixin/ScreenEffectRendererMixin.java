package net.pathdos.totemtweaks.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.pathdos.totemtweaks.config.Gui;
import org.joml.Quaternionfc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;


@Mixin(ScreenEffectRenderer.class)
public class ScreenEffectRendererMixin {

    @Shadow
    private ItemStack itemActivationItem;

    @Shadow
    private int itemActivationTicks;

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    private float itemActivationOffX;

    @Shadow
    private float itemActivationOffY;

    @Unique
    private int overlayTimeLeft;

    // Works?
    @Inject (method = "displayItemActivation", at = @At("TAIL"))
    public void InjectdisplayItemActivation(ItemStack stack, RandomSource random, CallbackInfo ci) {
      if (!Gui.get().TotemPopAnimation) {
            this.itemActivationTicks = 0;
        } else {
            this.itemActivationTicks = Gui.get().animationSpeed;
        }
        if (Gui.get().lockRotationPosition) {
            this.itemActivationOffX = 0;
            this.itemActivationOffY = 0;
        }
    }

//    @Inject(method = "renderFloatingItem", at = @At("TAIL"))
//    public void renderFloatingItemWithOverlays(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
//        if (Gui.get().showTotemCount && floatingItem != null && floatingItem.getItem() == Items.TOTEM_OF_UNDYING) {
//            int totemCount = getTotemCount();
//            String countText = "Totems: " + totemCount;
//            int screenWidth = MinecraftClient.getInstance().getWindow().getScaledWidth();
//            int screenHeight = MinecraftClient.getInstance().getWindow().getScaledHeight();
//            int textWidth = MinecraftClient.getInstance().textRenderer.getWidth(countText);
//            int x = (screenWidth - textWidth) / 2;
//            int y = screenHeight / 2 + 20;
//            context.drawText(MinecraftClient.getInstance().textRenderer, countText, x, y, 0xFFFFFF, true);
//            if (Gui.get().showOverlay && overlayTimeLeft > 0) {
//                int baseColor = Gui.get().overlayColor & 0x00FFFFFF;
//                int alpha = (int) ((Gui.get().overlayOpacity / 255.0) * (overlayTimeLeft / (float) Gui.get().animationSpeed) * 255);
//                int color = baseColor | (alpha << 24);
//                context.fill(0, 0, screenWidth, screenHeight, color);
//                overlayTimeLeft--;
//            }
//        }
//        if (Gui.get().showOverlay) {
//            overlayTimeLeft = floatingItemTimer;
//        }
//    }
/*    @Unique
    private int getTotemCount() {
        int count = 0;
        assert MinecraftClient.getInstance().player != null;
        for (ItemStack stack : MinecraftClient.getInstance().player.getInventory().getMainStacks()) {
            if (stack.getItem() == Items.TOTEM_OF_UNDYING) {
                count += stack.getCount();
            }
        }
        return count;
    }*/

    @ModifyVariable(method = "renderItemActivationAnimation", at = @At("STORE"), ordinal = 0)
    private int modifyTickRenderfloatingItem(int i) {
        return Gui.get().animationSpeed - itemActivationTicks;
    }

    @ModifyVariable(method = "renderItemActivationAnimation", at = @At("STORE"), ordinal = 1)
    private float modifyFloatRenderfloatingItem(float f) {
        return f * 40 / Gui.get().animationSpeed;
    }

    @Inject(
            method = "renderItemActivationAnimation",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V",
                    ordinal = 0,
                    shift = At.Shift.BEFORE
            )
    )
    private void moveTotemOrigin(PoseStack matrices, float tickProgress, SubmitNodeCollector queue, CallbackInfo ci, @Local(ordinal = 4) float k) {
        Minecraft client = Minecraft.getInstance();
        Window window = client.getWindow();
        matrices.setIdentity();
        float sliderX = Gui.get().xPosition;
        float sliderY = Gui.get().yPosition;

        float screenWidth = window.getGuiScaledWidth();
        float screenHeight = window.getGuiScaledHeight();


        float pixelX = (sliderX / 100f) * screenWidth;
        float pixelY = (sliderY / 100f) * screenHeight;

        float normalizedX = (pixelX - screenWidth / 2f) / (screenWidth / 2f);
        float normalizedY = (screenHeight / 2f - pixelY) / (screenHeight / 2f);
        System.out.println("X: " + normalizedX + " Y: " + normalizedY);
        matrices.translate(normalizedX * 7.5F, normalizedY * 7.5F, -10.0F + 9.0F * Mth.sin((double)k));
    }



    @ModifyArgs(method = "renderItemActivationAnimation", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;scale(FFF)V"))
    private void modifyScaleArgs(Args args) {
        float scale = 0.8F;

        scale *= Gui.get().popSize;

        args.set(0, scale);
        args.set(1, scale);
        args.set(2, scale);
    }


    @WrapWithCondition(method = "renderItemActivationAnimation", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;mulPose(Lorg/joml/Quaternionfc;)V", ordinal = 0))
    private boolean wrapRotationY(PoseStack matrixStack, Quaternionfc rotation) {
        return !Gui.get().disableRotations;
    }

    @WrapWithCondition(method = "renderItemActivationAnimation", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;mulPose(Lorg/joml/Quaternionfc;)V", ordinal = 1))
    private boolean wrapRotationX(PoseStack matrixStack, Quaternionfc rotation) {
        return !Gui.get().disableRotations;
    }

    @WrapWithCondition(method = "renderItemActivationAnimation", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;mulPose(Lorg/joml/Quaternionfc;)V", ordinal = 2))
    private boolean wrapRotationZ(PoseStack matrixStack, Quaternionfc rotation) {
        return !Gui.get().disableRotations;
    }
}