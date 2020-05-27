package me.mrCookieSlime.EmeraldEnchants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.mrCookieSlime.CSCoreLibPlugin.general.Block.TreeCalculator;
import me.mrCookieSlime.CSCoreLibPlugin.general.Block.Vein;
import me.mrCookieSlime.CSCoreLibPlugin.general.Player.PlayerInventory;
import me.mrCookieSlime.CSCoreLibPlugin.general.String.StringUtils;
import me.mrCookieSlime.EmeraldEnchants.actions.implementations.CarryAction;
import me.mrCookieSlime.EmeraldEnchants.actions.implementations.DamageAction;
import me.mrCookieSlime.EmeraldEnchants.actions.implementations.DigAction;
import me.mrCookieSlime.EmeraldEnchants.actions.implementations.HitAction;
import me.mrCookieSlime.EmeraldEnchants.actions.implementations.InteractAction;
import me.mrCookieSlime.EmeraldEnchants.actions.implementations.ProjectileHitAction;
import me.mrCookieSlime.EmeraldEnchants.actions.implementations.WearAction;

public final class EnchantmentSetup {
	
	private EnchantmentSetup() {}
	
	public static void setupDefaultEnchantments(EnchantmentRegistry registry, Random random) {
		Set<PotionEffectType> effectBlacklist = new HashSet<>();
		Map<PotionEffectType, String> aliases = new HashMap<>();
		effectBlacklist.add(PotionEffectType.HARM);
		effectBlacklist.add(PotionEffectType.HEAL);
		effectBlacklist.add(PotionEffectType.BAD_OMEN);
		effectBlacklist.add(PotionEffectType.CONDUIT_POWER);
		effectBlacklist.add(PotionEffectType.DOLPHINS_GRACE);
		effectBlacklist.add(PotionEffectType.HERO_OF_THE_VILLAGE);
		effectBlacklist.add(PotionEffectType.LEVITATION);
		effectBlacklist.add(PotionEffectType.BLINDNESS);
		effectBlacklist.add(PotionEffectType.CONFUSION);
		effectBlacklist.add(PotionEffectType.POISON);
		effectBlacklist.add(PotionEffectType.WITHER);
		
		aliases.put(PotionEffectType.CONFUSION, "NAUSEA");
		aliases.put(PotionEffectType.DAMAGE_RESISTANCE, "RESISTANCE");
		aliases.put(PotionEffectType.FAST_DIGGING, "HASTE");
		aliases.put(PotionEffectType.INCREASE_DAMAGE, "STRENGTH");
		aliases.put(PotionEffectType.JUMP, "JUMP_BOOST");
		aliases.put(PotionEffectType.SLOW, "SLOWNESS");
		aliases.put(PotionEffectType.SLOW_DIGGING, "MINING_FATIGUE");
		aliases.put(PotionEffectType.UNLUCK, "BAD_LUCK");
		
		registry.registerEnchantment("农民精神", new ItemStack(Material.WHEAT_SEEDS), 1, Arrays.asList(ApplicableItem.BOOTS), Arrays.asList("你再也不需要担心", "踩坏农作物"),
			new InteractAction() {

				@Override
				public void onInteract(int level, Player p, PlayerInteractEvent event) {
					if (event.getAction() == Action.PHYSICAL) event.setCancelled(true);
				}
			
			}
		);
		
		registry.registerEnchantment("矿脉挖掘者", new ItemStack(Material.DIAMOND_PICKAXE), 50, Arrays.asList(ApplicableItem.PICKAXE, ApplicableItem.AXE, ApplicableItem.SHOVEL), Arrays.asList("允许你一次挖掉", "所有相同的方块"),
			new DigAction() {

				@Override
				public void onDig(int level, Player p, Block block, List<ItemStack> drops) {
					PlayerInventory.damageItemInHand(p);
					List<Location> blocks = new ArrayList<>();
					Vein.calculate(block.getLocation(), block.getLocation(), blocks, level);
					blocks.remove(block.getLocation());
					for (Location l: blocks) {
						IgnoredMiningEvent event = new IgnoredMiningEvent(l.getBlock(), p);
						Bukkit.getPluginManager().callEvent(event);
						if (!event.isCancelled()) {
							l.getWorld().playEffect(l, Effect.STEP_SOUND, l.getBlock().getType());
							for (ItemStack item: l.getBlock().getDrops()) {
								drops.add(item);
							}
							l.getBlock().setType(Material.AIR);
						}
					}
				}
					
			}
		);
		
		registry.registerEnchantment("伐木工", new ItemStack(Material.DIAMOND_AXE), 50, Arrays.asList(ApplicableItem.AXE), Arrays.asList("允许您一次砍伐", "一棵树的所有原木"),
			new DigAction() {

				@Override
				public void onDig(int level, Player p, Block block, List<ItemStack> drops) {
					if (Tag.LOGS.isTagged(block.getType())) {
						PlayerInventory.damageItemInHand(p);
						List<Location> blocks = new ArrayList<>();
						TreeCalculator.getTree(block.getLocation(), block.getLocation(), blocks);
						blocks.remove(block.getLocation());
						for (Location l: blocks) {
							IgnoredMiningEvent event = new IgnoredMiningEvent(l.getBlock(), p);
							Bukkit.getPluginManager().callEvent(event);
							if (!event.isCancelled()) {
								l.getWorld().playEffect(l, Effect.STEP_SOUND, l.getBlock().getType());
								for (ItemStack item: l.getBlock().getDrops()) {
									drops.add(item);
								}
								l.getBlock().setType(Material.AIR);
							}
						}
					}
				}
					
			}
		);
		
		registry.registerEnchantment("极寒", new ItemStack(Material.ICE), 2, Arrays.asList(ApplicableItem.SWORD, ApplicableItem.AXE, ApplicableItem.BOW), Arrays.asList("减慢并削弱你的", "敌人"),
			new HitAction() {
				
				@Override
				public void onHit(int level, Player p, EntityDamageByEntityEvent e) {
					if (!(e.getEntity() instanceof LivingEntity)) return;
					if (level == 1) {
						if (random.nextInt(10) < 4) {
							((LivingEntity) e.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 0));
						}
					}
					else if (random.nextInt(10) < 5) {
						((LivingEntity) e.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 200, 0));
						((LivingEntity) e.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 200, 0));
					}
				}
			},
			new ProjectileHitAction() {

				@Override
				public void onHit(int level, EntityDamageByEntityEvent e) {
					if (!(e.getEntity() instanceof LivingEntity)) return;
					if (level == 1) {
						if (random.nextInt(10) < 4) ((LivingEntity) e.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 0));
					}
					else if (random.nextInt(10) < 5) {
						((LivingEntity) e.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 200, 0));
						((LivingEntity) e.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 200, 0));
					}
				}
				
			}
		);
		
		registry.registerEnchantment("吸血鬼", new ItemStack(Material.WITHER_SKELETON_SKULL), 4, Arrays.asList(ApplicableItem.SWORD, ApplicableItem.AXE), Arrays.asList("吸收对手生命值的一小部分"),
			new HitAction() {
				
				@Override
				public void onHit(int level, Player p, EntityDamageByEntityEvent e) {
					if (!(e.getEntity() instanceof LivingEntity)) return;
					LivingEntity n = (LivingEntity) e.getEntity();
					double health = p.getHealth() + (n.getHealth() / 20 * level);
					if (health > p.getMaxHealth()) {
						health = p.getMaxHealth();
					}
					p.setHealth(health);
				}
			}
		);
		
		registry.registerEnchantment("经验偷取", new ItemStack(Material.EXPERIENCE_BOTTLE), 5, Arrays.asList(ApplicableItem.SWORD, ApplicableItem.AXE), Arrays.asList("在你攻击的对手身上", "偷取一小部分经验值"),
			new HitAction() {
				
				@Override
				public void onHit(int level, Player p, EntityDamageByEntityEvent e) {
					if (!(e.getEntity() instanceof Player)) return;
					Player victim = (Player) e.getEntity();
					int xp = level * (10 + random.nextInt(50));
					if (xp > victim.getTotalExperience() && victim.getTotalExperience() > 0) {
						xp = victim.getTotalExperience();
					}
					
					if (victim.getTotalExperience() >= xp) {
						p.giveExp(xp);
						victim.setTotalExperience(victim.getTotalExperience() - xp);
					}
				}
			}
		);
		
		registry.registerEnchantment("抢食", new ItemStack(Material.ROTTEN_FLESH), 4, Arrays.asList(ApplicableItem.SWORD, ApplicableItem.AXE), Arrays.asList("吸收对手一小部分饥饿值"),
			new HitAction() {
				
				@Override
				public void onHit(int level, Player p, EntityDamageByEntityEvent e) {
					if (!(e.getEntity() instanceof Player)) return;
					Player n = (Player) e.getEntity();
					if (p.getFoodLevel() < 20) {
						int food = n.getFoodLevel() / 20 * level;
						if (food > 0) {
							if (food > 20 - p.getFoodLevel()) food = 20 - p.getFoodLevel();
							p.setFoodLevel(p.getFoodLevel() + food);
							n.setFoodLevel(n.getFoodLevel() - food > 0 ? n.getFoodLevel() - food: 0);
						}
					}
				}
			}
		);
		
		registry.registerEnchantment("箭头反弹", new ItemStack(Material.ARROW), 10, Arrays.asList(ApplicableItem.BOOTS, ApplicableItem.LEGGINGS, ApplicableItem.CHESTPLATE, ApplicableItem.HELMET), Arrays.asList("反弹箭头"),
			new DamageAction() {

				@Override
				public void onDamage(int level, Player p, EntityDamageEvent e) {
					if (!(e.getEntity() instanceof EntityDamageByEntityEvent)) return;
					if (!(((EntityDamageByEntityEvent) e).getDamager() instanceof Arrow)) return;
					
					if (random.nextInt(10) < level) e.setCancelled(true);
				}
			}
		);
		
		registry.registerEnchantment("毒液", new ItemStack(Material.SPIDER_EYE), 2, Arrays.asList(ApplicableItem.SWORD, ApplicableItem.AXE, ApplicableItem.BOW), Arrays.asList("使你的敌人中毒"),
			new HitAction() {
				
				@Override
				public void onHit(int level, Player p, EntityDamageByEntityEvent e) {
					if (!(e.getEntity() instanceof LivingEntity)) return;
					if (level == 1) {
						if (random.nextInt(10) < 4) ((LivingEntity) e.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 0));
					}
					else if (random.nextInt(10) < 5) {
						((LivingEntity) e.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.POISON, 200, 0));
					}
				}
			},
			new ProjectileHitAction() {

				@Override
				public void onHit(int level, EntityDamageByEntityEvent e) {
					if (!(e.getEntity() instanceof LivingEntity)) return;
					if (level == 1) {
						if (random.nextInt(10) < 4) ((LivingEntity) e.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 0));
					}
					else if (random.nextInt(10) < 5) {
						((LivingEntity) e.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.POISON, 200, 0));
					}
				}
				
			}
		);
		
		registry.registerEnchantment("僵尸流感", new ItemStack(Material.ZOMBIE_HEAD), 2, Arrays.asList(ApplicableItem.SWORD, ApplicableItem.AXE, ApplicableItem.BOW), Arrays.asList("使你的敌人饥饿"),
			new HitAction() {
				
				@Override
				public void onHit(int level, Player p, EntityDamageByEntityEvent e) {
					if (!(e.getEntity() instanceof LivingEntity)) return;
					if (level == 1) {
						if (random.nextInt(10) < 4) ((LivingEntity) e.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 100, 0));
					}
					else if (random.nextInt(10) < 5) {
						((LivingEntity) e.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 200, 0));
					}
				}
			},
			new ProjectileHitAction() {

				@Override
				public void onHit(int level, EntityDamageByEntityEvent e) {
					if (!(e.getEntity() instanceof LivingEntity)) return;
					if (level == 1) {
						if (random.nextInt(10) < 4) ((LivingEntity) e.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 100, 0));
					}
					else if (random.nextInt(10) < 5) {
						((LivingEntity) e.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 200, 0));
					}
				}
				
			}
		);
		
		registry.registerEnchantment("隐藏", new ItemStack(Material.BLACK_WOOL), 2, Arrays.asList(ApplicableItem.SWORD, ApplicableItem.AXE, ApplicableItem.BOW), Arrays.asList("使你的敌人致盲"),
			new HitAction() {
				
				@Override
				public void onHit(int level, Player p, EntityDamageByEntityEvent e) {
					if (!(e.getEntity() instanceof LivingEntity)) return;
					if (level == 1) {
						if (random.nextInt(10) < 4) ((LivingEntity) e.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 0));
					}
					else if (random.nextInt(10) < 5) {
						((LivingEntity) e.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 200, 0));
					}
				}
			},
			new ProjectileHitAction() {

				@Override
				public void onHit(int level, EntityDamageByEntityEvent e) {
					if (!(e.getEntity() instanceof LivingEntity)) return;
					if (level == 1) {
						if (random.nextInt(10) < 4) ((LivingEntity) e.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 0));
					}
					else if (random.nextInt(10) < 5) {
						((LivingEntity) e.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 200, 0));
					}
				}
				
			}
		);
		
		registry.registerEnchantment("眩晕", new ItemStack(Material.MELON_SEEDS), 2, Arrays.asList(ApplicableItem.SWORD, ApplicableItem.AXE, ApplicableItem.BOW), Arrays.asList("使你的敌人晕眩"),
			new HitAction() {
				
				@Override
				public void onHit(int level, Player p, EntityDamageByEntityEvent e) {
					if (!(e.getEntity() instanceof LivingEntity)) return;
					if (level == 1) {
						if (random.nextInt(10) < 4) ((LivingEntity) e.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 100, 0));
					}
					else if (random.nextInt(10) < 5) {
						((LivingEntity) e.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 200, 0));
					}
				}
			},
			new ProjectileHitAction() {

				@Override
				public void onHit(int level, EntityDamageByEntityEvent e) {
					if (!(e.getEntity() instanceof LivingEntity)) return;
					if (level == 1) {
						if (random.nextInt(10) < 4) ((LivingEntity) e.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 100, 0));
					}
					else if (random.nextInt(10) < 5) {
						((LivingEntity) e.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 200, 0));
					}
				}
				
			}
		);
		
		registry.registerEnchantment("凋零", new ItemStack(Material.WITHER_SKELETON_SKULL), 2, Arrays.asList(ApplicableItem.SWORD, ApplicableItem.AXE), Arrays.asList("给予你的敌人凋零效果"),
			new HitAction() {
				
				@Override
				public void onHit(int level, Player p, EntityDamageByEntityEvent e) {
					if (!(e.getEntity() instanceof LivingEntity)) return;
					if (level == 1) {
						if (random.nextInt(10) < 4) ((LivingEntity) e.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 100, 0));
					}
					else if (random.nextInt(10) < 5) {
						((LivingEntity) e.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 200, 0));
					}
				}
			}
		);
		
		registry.registerEnchantment("炙热", new ItemStack(Material.LAVA_BUCKET), 4, Arrays.asList(ApplicableItem.BOOTS, ApplicableItem.LEGGINGS, ApplicableItem.CHESTPLATE, ApplicableItem.HELMET), Arrays.asList("使攻击者着火"),
			new DamageAction() {

				@Override
				public void onDamage(int level, Player p, EntityDamageEvent e) {
					if (!(e.getEntity() instanceof EntityDamageByEntityEvent)) return;
					Entity n = ((EntityDamageByEntityEvent) e).getDamager();
					if (!(n instanceof LivingEntity)) return;
					if (random.nextInt(100) < 25 + level * 25) {
						n.setFireTicks(20 * (4 + level * 2));
					}
				}
			}
		);
		
		registry.registerEnchantment("魔法保护", new ItemStack(Material.BLAZE_POWDER), 4, Arrays.asList(ApplicableItem.BOOTS, ApplicableItem.LEGGINGS, ApplicableItem.CHESTPLATE, ApplicableItem.HELMET), Arrays.asList("抵御瞬间伤害效果"),
			new DamageAction() {

				@Override
				public void onDamage(int level, Player p, EntityDamageEvent e) {
					if (e.getCause() == DamageCause.MAGIC) e.setDamage(e.getDamage() / (level + 1));
				}
			}
		);
		
		registry.registerEnchantment("中毒保护", new ItemStack(Material.POISONOUS_POTATO), 4, Arrays.asList(ApplicableItem.BOOTS, ApplicableItem.LEGGINGS, ApplicableItem.CHESTPLATE, ApplicableItem.HELMET), Arrays.asList("抵御中毒效果"),
			new DamageAction() {

				@Override
				public void onDamage(int level, Player p, EntityDamageEvent e) {
					if (e.getCause() == DamageCause.POISON) e.setDamage(e.getDamage() / (level + 1));
				}
			}
		);
		
		registry.registerEnchantment("凋零保护", new ItemStack(Material.WITHER_SKELETON_SKULL), 4, Arrays.asList(ApplicableItem.BOOTS, ApplicableItem.LEGGINGS, ApplicableItem.CHESTPLATE, ApplicableItem.HELMET), Arrays.asList("抵御凋零效果"),
			new DamageAction() {

				@Override
				public void onDamage(int level, Player p, EntityDamageEvent e) {
					if (e.getCause() == DamageCause.WITHER) e.setDamage(e.getDamage() / (level + 1));
				}
			}
		);
		
		registry.registerEnchantment("末影珍珠保护", new ItemStack(Material.ENDER_PEARL), 2, Arrays.asList(ApplicableItem.BOOTS, ApplicableItem.LEGGINGS, ApplicableItem.CHESTPLATE, ApplicableItem.HELMET), Arrays.asList("抵御扔出末影珍珠后造成的伤害"),
			new DamageAction() {

				@Override
				public void onDamage(int level, Player p, EntityDamageEvent e) {
					if (e instanceof EntityDamageByEntityEvent && ((EntityDamageByEntityEvent) e).getDamager() instanceof EnderPearl) {
						e.setDamage(e.getDamage() / (level + 1));
					}
				}
			}
		);
		
		registry.registerEnchantment("刺伤保护", new ItemStack(Material.CACTUS), 4, Arrays.asList(ApplicableItem.BOOTS, ApplicableItem.LEGGINGS, ApplicableItem.CHESTPLATE, ApplicableItem.HELMET), Arrays.asList("保护免受仙人掌和灌木丛的伤害"),
			new DamageAction() {

				@Override
				public void onDamage(int level, Player p, EntityDamageEvent e) {
					if (e.getCause() == DamageCause.CONTACT) {
						e.setDamage(e.getDamage() / (level + 1));
					}
				}
			}
		);
		
		registry.registerEnchantment("磁铁", new ItemStack(Material.HOPPER), 10, Arrays.asList(ApplicableItem.BOOTS, ApplicableItem.LEGGINGS, ApplicableItem.CHESTPLATE, ApplicableItem.HELMET), Arrays.asList("穿着时自动拾取附近的所有物品"),
			new WearAction() {

				@Override
				public void onWear(int level, Player p, int delay) {
					boolean sound = false;
					for (Entity n: p.getNearbyEntities(level, level, level)) {
						if (n instanceof Item && !n.hasMetadata("no_pickup")) {
							n.teleport(p);
							sound = true;
						}
					}
					if (sound) p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 5F, 2F);
				}
			
			}
		);
		
		registry.registerEnchantment("铁头", new ItemStack(Material.ANVIL), 4, Arrays.asList(ApplicableItem.HELMET), Arrays.asList("保护免受掉落方块所造成", "的伤害(比如铁砧)"),
			new DamageAction() {

				@Override
				public void onDamage(int level, Player p, EntityDamageEvent e) {
					if (e.getCause() == DamageCause.FALLING_BLOCK) e.setDamage(e.getDamage() / (level + 1));
				}
			}
		);

		HashMap<String, String> alias = new HashMap<>();
		alias.put("Speed", "速度");
		alias.put("Slowness", "缓慢");
		alias.put("Haste", "急迫");
		alias.put("Mining Fatigue", "挖掘疲劳");
		alias.put("Strength", "力量");
		alias.put("Instant Health", "瞬间治疗");
		alias.put("Instant Damage", "瞬间伤害");
		alias.put("Jump Boost", "跳跃提升");
		alias.put("Nausea", "反胃");
		alias.put("Regeneration", "生命恢复");
		alias.put("Resistance", "抗性提升");
		alias.put("Fire Resistance", "防火");
		alias.put("Water Breathing", "水下呼吸");
		alias.put("Invisibility", "隐身");
		alias.put("Blindness", "致盲");
		alias.put("Night Vision", "夜视");
		alias.put("Hunger", "饥饿");
		alias.put("Weakness", "虚弱");
		alias.put("Poison", "中毒");
		alias.put("Wither", "凋零");
		alias.put("Health Boost", "生命提升");
		alias.put("Absorption", "伤害吸收");
		alias.put("Saturation", "饱和");
		alias.put("Glowing", "发光");
		alias.put("Levitation", "漂浮");
		alias.put("Luck", "幸运");
		alias.put("Bad Luck", "霉运");
		alias.put("Heal", "治疗");
		alias.put("Harm", "伤害");
		alias.put("Bad Omen", "不祥之兆");
		alias.put("Conduit Power", "潮涌能量");
		alias.put("Dolphins Grace", "海豚的恩惠");
		alias.put("Hero Of The Village", "村庄英雄");
		alias.put("Slow Falling", "缓降");

		for (PotionEffectType type: PotionEffectType.values()) {
			if (type != null) {
				String name = aliases.containsKey(type) ? aliases.get(type): type.getName();
				
				ItemStack item = new ItemStack(Material.POTION);
				PotionMeta meta = (PotionMeta) item.getItemMeta();
				meta.setColor(type.getColor());
				meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
				item.setItemMeta(meta);
				
				EmeraldEnchants.getInstance().getCfg().setDefaultValue(name + ".enabled", !effectBlacklist.contains(type));
				registry.registerEnchantment(alias.get(StringUtils.format(name)), item, 3, Arrays.asList(ApplicableItem.values()), Arrays.asList("当你穿着或拿着带有该附魔效果", "的物品时给予你药水效果", "\"" + alias.get(StringUtils.format(name)) + "\""),
					new CarryAction() {
	
						@Override
						public void onCarry(int level, Player p, int delay) {
							final double health = p.getHealth();
							if (p.hasPotionEffect(type)) p.removePotionEffect(type);
							p.addPotionEffect(new PotionEffect(type, delay * 20 + 100, level - 1));
							if (health <= p.getMaxHealth()) {
								p.setHealth(health);
							}
						}
							
					},
					new WearAction() {
	
						@Override
						public void onWear(int level, Player p, int delay) {
							final double health = p.getHealth();
							if (p.hasPotionEffect(type)) p.removePotionEffect(type);
							p.addPotionEffect(new PotionEffect(type, delay * 20 + 100, level - 1));
							if (health <= p.getMaxHealth()) {
								p.setHealth(health);
							}
						}
									
					}
				);
			}
		}
	}

}
