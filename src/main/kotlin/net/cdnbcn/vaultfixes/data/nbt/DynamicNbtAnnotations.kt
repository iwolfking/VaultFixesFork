package net.cdnbcn.vaultfixes.data.nbt

@Target(AnnotationTarget.FIELD)
annotation class DynNbtIgnore()
@Target(AnnotationTarget.FIELD)
annotation class DynNbtName(val name: String)
@Target(AnnotationTarget.CLASS)
annotation class DynNbt()