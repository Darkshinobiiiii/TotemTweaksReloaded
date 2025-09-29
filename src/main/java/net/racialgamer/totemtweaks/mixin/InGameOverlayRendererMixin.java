package net.racialgamer.totemtweaks.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.racialgamer.totemtweaks.config.Gui;
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


@Mixin(InGameOverlayRenderer.class)
public class InGameOverlayRendererMixin {

    @Shadow
    private ItemStack floatingItem;

    @Shadow
    private int floatingItemTimer;

    @Shadow
    @Final
    private MinecraftClient client;

    @Shadow
    private float floatingItemOffsetX;

    @Shadow
    private float floatingItemOffsetY;

    @Unique
    private int overlayTimeLeft;

    // Works?
    @Inject (method = "setFloatingItem", at = @At("TAIL"))
    public void InjectSetFloatingItem(ItemStack stack, Random random, CallbackInfo ci) {
      if (!Gui.get().TotemPopAnimation) {
            this.floatingItemTimer = 0;
        } else {
            this.floatingItemTimer = Gui.get().animationSpeed;
        }
        if (Gui.get().lockRotationPosition) {
            this.floatingItemOffsetX = 0;
            this.floatingItemOffsetY = 0;
        }
        if (Gui.get().showOverlay) {
            this.overlayTimeLeft = Gui.get().animationSpeed;
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

    @Unique
    private int getTotemCount() {
        int count = 0;
        assert MinecraftClient.getInstance().player != null;
        for (ItemStack stack : MinecraftClient.getInstance().player.getInventory().getMainStacks()) {
            if (stack.getItem() == Items.TOTEM_OF_UNDYING) {
                count += stack.getCount();
            }
        }
        return count;
    }

    @ModifyVariable(method = "renderFloatingItem", at = @At("STORE"), ordinal = 0)
    private int modifyTickRenderfloatingItem(int i) {
        return Gui.get().animationSpeed - floatingItemTimer;
    }

    @ModifyVariable(method = "renderFloatingItem", at = @At("STORE"), ordinal = 1)
    private float modifyFloatRenderfloatingItem(float f) {
        return f * 40 / Gui.get().animationSpeed;
    }

    @ModifyArgs(method = "renderFloatingItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;translate(FFF)V", ordinal = 0))
    private void modifyTranslateArgs(Args args, @Local(ordinal = 5) float k) {
        float x = args.get(0);
        float y = args.get(1);

        float screenWidth = MinecraftClient.getInstance().getWindow().getScaledWidth();
        float screenHeight = MinecraftClient.getInstance().getWindow().getScaledHeight();

        float l = floatingItemOffsetX * (screenWidth / 4);
        float m = floatingItemOffsetY * (screenHeight / 4);
        float fixedX = (screenWidth / 2) + l * MathHelper.abs(MathHelper.sin(k * 2.0F));
        float fixedY = (screenHeight / 2) + m * MathHelper.abs(MathHelper.sin(k * 2.0F));
        float adjustedX = fixedX + ((Gui.get().xPosition - 50) / 100.0f * screenWidth);
        float adjustedY = fixedY + ((Gui.get().yPosition - 50) / 100.0f * screenHeight);

        args.set(0, adjustedX);
        args.set(1, adjustedY);

        if (Gui.get().staticSize) {
            float z = 0F; // -125F - 225F
            args.set(2, z);
        }
    }

    @ModifyArgs(method = "renderFloatingItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;scale(FFF)V"))
    private void modifyScaleArgs(Args args) {
        float scale = 0.8F;

        scale *= Gui.get().popSize;

        args.set(0, scale);
        args.set(1, scale);
        args.set(2, scale);
    }


    @WrapWithCondition(method = "renderFloatingItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;multiply(Lorg/joml/Quaternionfc;)V", ordinal = 0))
    private boolean wrapRotationY(MatrixStack matrixStack, Quaternionfc rotation) {
        return !Gui.get().disableRotations;
    }

    @WrapWithCondition(method = "renderFloatingItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;multiply(Lorg/joml/Quaternionfc;)V", ordinal = 1))
    private boolean wrapRotationX(MatrixStack matrixStack, Quaternionfc rotation) {
        return !Gui.get().disableRotations;
    }

    @WrapWithCondition(method = "renderFloatingItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;multiply(Lorg/joml/Quaternionfc;)V", ordinal = 2))
    private boolean wrapRotationZ(MatrixStack matrixStack, Quaternionfc rotation) {
        return !Gui.get().disableRotations;
    }
}