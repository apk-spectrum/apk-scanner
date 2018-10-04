package com.apkscanner.plugin.manifest;

public class Component
{
	static public final int TYPE_UNKNWON = -1;
	static public final int TYPE_PLUGIN_GROUP = 0;
	static public final int TYPE_PACAKGE_SEARCHER = 1;
	static public final int TYPE_PACAKGE_SEARCHER_LINKER = 2;
	static public final int TYPE_UPDATE_CHECKER = 3;
	static public final int TYPE_UPDATE_CHECKER_LINKER = 4;
	static public final int TYPE_EXTERNAL_TOOL = 5;
	static public final int TYPE_EXTERNAL_TOOL_LINKER = 6;
	static public final int TYPE_EXTRA_COMPONENT = 7;

	public final int type;
	public final String description;
	public final boolean enabled;
	public final String icon;
	public final String label;
	public final String name;
	public final String url;

	//public final Linker[] linkers;
	public final String target;
	public final String preferLang;
	public final String path;
	public final String param;
	public final String updateUrl;
	public final String pluginGroup;
	public final String like;
	public final String supportedOS;
	public final Boolean visibleToBasic;
	public final String periodDay;

	Component(int type, boolean enable, String label, String icon, String description, String name, String url, /* Linker[] linkers */
			String target, String preferLang, String path, String param, String updateUrl, String pluginGroup, String like, String supportedOS,
			Boolean visibleToBasic, String periodDay) {
		this.type = type;
		this.enabled = enable;
		this.label = label;
		this.icon = icon;
		this.description = description;
		this.name = name;
		this.url = url;

		//this.linkers = linkers;
		this.target = target;
		this.preferLang = preferLang;
		this.path = path;
		this.param = param;
		this.updateUrl = updateUrl;
		this.pluginGroup = pluginGroup;
		this.like = like;
		this.supportedOS = supportedOS;
		this.visibleToBasic = visibleToBasic;
		this.periodDay = periodDay;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + (enabled ? 1231 : 1237);
		result = prime * result + ((icon == null) ? 0 : icon.hashCode());
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		result = prime * result + ((like == null) ? 0 : like.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((param == null) ? 0 : param.hashCode());
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		result = prime * result + ((pluginGroup == null) ? 0 : pluginGroup.hashCode());
		result = prime * result + ((preferLang == null) ? 0 : preferLang.hashCode());
		result = prime * result + ((supportedOS == null) ? 0 : supportedOS.hashCode());
		result = prime * result + ((target == null) ? 0 : target.hashCode());
		result = prime * result + type;
		result = prime * result + ((updateUrl == null) ? 0 : updateUrl.hashCode());
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		result = prime * result + ((visibleToBasic == null) ? 0 : (visibleToBasic ? 1231 : 1237));
		result = prime * result + ((periodDay == null) ? 0 : periodDay.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Component other = (Component) obj;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (enabled != other.enabled)
			return false;
		if (icon == null) {
			if (other.icon != null)
				return false;
		} else if (!icon.equals(other.icon))
			return false;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		if (like == null) {
			if (other.like != null)
				return false;
		} else if (!like.equals(other.like))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (param == null) {
			if (other.param != null)
				return false;
		} else if (!param.equals(other.param))
			return false;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		if (pluginGroup == null) {
			if (other.pluginGroup != null)
				return false;
		} else if (!pluginGroup.equals(other.pluginGroup))
			return false;
		if (preferLang == null) {
			if (other.preferLang != null)
				return false;
		} else if (!preferLang.equals(other.preferLang))
			return false;
		if (supportedOS == null) {
			if (other.supportedOS != null)
				return false;
		} else if (!supportedOS.equals(other.supportedOS))
			return false;
		if (target == null) {
			if (other.target != null)
				return false;
		} else if (!target.equals(other.target))
			return false;
		if (type != other.type)
			return false;
		if (updateUrl == null) {
			if (other.updateUrl != null)
				return false;
		} else if (!updateUrl.equals(other.updateUrl))
			return false;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		if (visibleToBasic == null) {
			if (other.visibleToBasic != null)
				return false;
		} else if (!visibleToBasic.equals(other.visibleToBasic))
			return false;
		if (periodDay == null) {
			if (other.periodDay != null)
				return false;
		} else if (!periodDay.equals(other.periodDay))
			return false;
		return true;
	}
}
