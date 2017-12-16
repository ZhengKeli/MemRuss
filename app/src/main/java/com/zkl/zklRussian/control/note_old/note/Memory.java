package com.zkl.zklRussian.control.note_old.note;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class Memory {
	public static class MemoryStatistics {
		public final long learning;
		public final long finished;
		public final long notFilled;

		public MemoryStatistics(long learning, long finished, long notFilled) {
			this.learning = learning;
			this.finished = finished;
			this.notFilled = notFilled;
		}
	}

	public abstract static class MemoryBook extends Notebook {
		protected MemoryBook(@NonNull BookInfo bookInfo, @Nullable MemoryPlan memoryPlan) {
			super(bookInfo);
			this.memoryPlan=memoryPlan;
		}
		@Override public boolean rawCopy(Notebook notebook) {
			boolean succeed=super.rawCopy(notebook);
			if (notebook instanceof MemoryBook) {
				succeed &= rawModifyMemoryPlan(((MemoryBook) notebook).getMemoryPlan());
			}
			return succeed;
		}

		//memoryPlan
		private MemoryPlan memoryPlan=null;
		@Nullable
		public MemoryPlan getMemoryPlan(){ return memoryPlan; }
		@Nullable
		public PlanProgress getPlanProgress(){
			return getMemoryPlan() == null ? null : getMemoryPlan().progress;
		}
		@Nullable
		public PlanArgs getPlanArgs(){
			return getMemoryPlan() == null ? null : getMemoryPlan().args;
		}
		public boolean isMemoryPlanEnabled() {
			return getMemoryPlan() != null &&
				getMemoryPlan().isEnabled();
		}

		public abstract boolean onModifyMemoryPlan(@Nullable MemoryPlan memoryPlan);
		synchronized protected boolean mModifyMemoryPlan(@Nullable MemoryPlan memoryPlan) {
			if (onModifyMemoryPlan(memoryPlan)) {
				this.memoryPlan=memoryPlan;
				return true;
			}
			return false;
		}
		/**
		 * 修改计划的进度，不修改参数
		 */private boolean modifyPlanProgress(PlanProgress memoryProgress){
			if(getMemoryPlan()==null) return false;
			MemoryPlan newPlan = getMemoryPlan().setProgress(memoryProgress);
			return mModifyMemoryPlan(newPlan);
		}
		/**
		 * 修改计划的进度，不修改参数
		 */private boolean modifyPlanProgressWorkLoad(float workLoad) {
			return getPlanProgress() != null && modifyPlanProgress(getPlanProgress().setWorkLoad(workLoad));
		}
		/**
		 * 修改计划的进度，不修改参数
		 */private boolean modifyPlanProgressWorkLoadByOffset(float workLoadOffset){
			return getPlanProgress() != null && modifyPlanProgress(getPlanProgress().increaseWorkLoad(workLoadOffset));
		}
		/**
		 * 仅修改计划的参数，不改进度
		 */public boolean modifyMemoryPlanArgs(@Nullable PlanArgs args) {
			if(getMemoryPlan()==null) return false;
			MemoryPlan newMemoryPlan = (args == null) ? null : getMemoryPlan().setArgs(args);
			return mModifyMemoryPlan(newMemoryPlan);
		}
		/**
		 * 制定新的计划，改变了参数，并从新开始进度
		 */public boolean createMemoryPlan(PlanArgs args,long nowTime) {
			boolean planModifySuccess;
			if(args!=null) {
				planModifySuccess= mModifyMemoryPlan(new MemoryPlan(args, nowTime));
			}else {
				planModifySuccess= mModifyMemoryPlan(null);
			}
			if(planModifySuccess) {
				boolean notesModifySuccess= mClearAllNotesMemory();
				if (notesModifySuccess) {
					recountWorkLoad();
					return true;
				}else {
					throw new LostItemException(null);
				}
			}
			return false;
		}
		/**
		 * 放弃原来的学习计划
		 */public boolean clearMemoryPlan(){ return createMemoryPlan(null, 0); }

		/**
		 * 生操作修改MemoryPlan（注意，此时workLoad的统计将不被监控！）
		 */public boolean rawModifyMemoryPlan(MemoryPlan memoryPlan) {
			return mModifyMemoryPlan(memoryPlan);
		}

		//plan process
		abstract protected NoteStream getNotFilledNotes(int limit);
		/**
		 * 由底层进行的将单词加入计划的操作，此时会顺便计算workLoad
		 * @param maxCount 添加词的最多个数
		 * @return 实际添加进去的词的数量
		 */private synchronized int mFillMemoryNotes(int maxCount, long nowTime){
			assert getMemoryPlan()!=null;
			NoteStream toFills = getNotFilledNotes(maxCount);
			toFills.begin();
			int filled=0;
			while (toFills.goNext()) {
				LocalNote note = toFills.get();
				if(modifyNoteMemory(note, getBeginProgress(nowTime))){
					filled++;
				}
			}
			return filled;
		}
		public synchronized int checkRefillMemoryNotes(long nowTime,boolean considerWorkLoadLimit) {
			int filled=0;
			if (isMemoryPlanEnabled()) {
				assert getMemoryPlan() != null;//memoryPlanEnabled应该有保证
				int maxCount = getMemoryPlan().computeRefillCount(nowTime);
				if (considerWorkLoadLimit) {
					int workLoadLimit= (int) Math.floor(getMemoryPlan().getFreedWorkLoad()/ singleNewWordWorkLoad);
					maxCount = Math.min(maxCount, workLoadLimit);
				}
				if (maxCount < 0) {
					maxCount=0;
				}
				filled= mFillMemoryNotes(maxCount, nowTime);
				PlanProgress newProgress=getMemoryPlan().progress.getRefilled(getMemoryPlan().args,nowTime);
				if(!modifyPlanProgress(newProgress)){
					throw new LostItemException(newProgress);
				}
			}
			return filled;
		}
		public synchronized void fillMemoryNoteForced(int maxCount, long nowTime) {
			mFillMemoryNotes(maxCount, nowTime);
		}
		public boolean resultNextMemoryNote(NoteProgress progress) {
			if(getMemoryPlan()==null) return false;
			LocalNote nextMemoryNote = getNextMemoryNote();
			if (progress.progress >= getMemoryPlan().args.arg_times * Memory.progress_unit) {
				progress = Memory.getFinishProgress();
			}
			return nextMemoryNote != null && modifyNoteMemory(nextMemoryNote, progress);
		}

		//plan _name
		/**
		 * @return 单词学习进度的统计数据
		 */public abstract MemoryStatistics getMemoryStatistics();
		public boolean existNoteToFill() {
			return getMemoryStatistics().notFilled > 0;
		}
		protected abstract float onRecountWorkLoad();
		protected float mRecountWorkLoad(){
			if (getMemoryPlan() != null) {
				this.modifyPlanProgressWorkLoad(onRecountWorkLoad());
				return getMemoryPlan().progress.workLoad;
			}
			return 0;
		}
		public float recountWorkLoad(){
			return mRecountWorkLoad();
		}

		/**
		 * @param maxProgress 大于该进度的单词将被认为已经不需要再复习
		 * @return 计划内的下一个（时间上最早的）要复习的单词
		 */@Nullable
		abstract protected LocalNote getNextMemoryNote(int maxProgress);
		@Nullable
		public LocalNote getNextMemoryNote(){
			if (getMemoryPlan() != null) {
				return getNextMemoryNote((int) (Memory.progress_unit * getMemoryPlan().args.arg_times));
			}else{
				return null;
			}
		}
		/**
		 * @param nowTime 现在的UTC时间
		 * @return 计划内的已经到复习时间的，最早要复习的单词
		 */@Nullable
		public LocalNote getNextMemoryNoteInTime(long nowTime) {
			if (getMemoryPlan() != null) {
				LocalNote re = getNextMemoryNote();
				if (re != null && re.getNextTime() < getMemoryPlan().getNeededMemoryTimeLine(nowTime)) {
					return re;
				}
			}
			return null;
		}
		/**
		 * @return 返回下一次需要记忆词条的现在的时间，若该时间小于nowTime则会返回nowTime，返回-1表示没有需要记忆的词条了。
		 */public long getNextMemoryTime(long nowTime,boolean considerWorkLoadLimit) {
			long nextTime = -1;
			checkRefillMemoryNotes(nowTime,considerWorkLoadLimit);
			LocalNote nextNote = getNextMemoryNote();
			if (nextNote != null) {
				nextTime = nextNote.getNextTime();
				if (nextTime < nowTime) nextTime = nowTime;
			}

			if (getMemoryPlan() != null) {
				long nextRefillTime = getMemoryPlan().getNextRefillTime(nowTime);
				if ((nextTime > nextRefillTime || nextTime == -1) && nextRefillTime != 0) {
					if (existNoteToFill()) {
						nextTime = nextRefillTime;
					}
				}
			}

			return nextTime;
		}



		//_name resources initialize & release
		abstract public void prepareResourcesForMemory();
		abstract public void releaseResourcesForMemory();



		//_name memory modify
		/**
		 * @return 表示操作是否成功，true表示成功
		 */abstract protected boolean onModifyNoteMemory(LocalNote note, NoteProgress progress);
		/**
		 * @return 表示操作是否成功，true表示成功
		 */abstract protected boolean onClearAllNotesMemory();

		/**
		 * modifyNoteMemory操作的中介函数，用于处理内部事务，考虑了workLoad的变化
		 * @return 表示操作是否成功，true表示成功
		 */synchronized protected boolean mModifyNoteMemory(LocalNote note, NoteProgress progress){
			boolean success= onModifyNoteMemory(note, progress);
			if (getMemoryPlan() != null) {
				float rate=note.getNoteBody().getWorkLoadRate();
				float oldLoad = PlanProgress.computeNoteWorkLoad(getMemoryPlan().args,note.getNoteProgress(),rate);
				float newLoad = PlanProgress.computeNoteWorkLoad(getMemoryPlan().args,progress,rate);
				if(success){
					modifyPlanProgressWorkLoadByOffset(-oldLoad+newLoad);
				}
			}
			return success;
		}
		/**
		 * clearAllNoteMemory操作的中介函数，用于处理内部事务，考虑了workLoad变化
		 */synchronized protected boolean mClearAllNotesMemory(){
			if(onClearAllNotesMemory()){
				modifyPlanProgressWorkLoad(0);
				return true;
			}
			return false;
		}

		/**
		 * @return 表示操作是否成功，true表示成功
		 */public boolean modifyNoteMemory(LocalNote note, NoteProgress progress) {
			return mModifyNoteMemory(note, progress);
		}
		/**
		 * @return 表示操作是否成功，true表示成功
		 */public boolean clearNoteMemory(LocalNote note) {
			return modifyNoteMemory(note, null);
		}


		//_name add & delete
		/**
		 * add操作的中介方法，用于处理内部事务，考虑了workLoad的变化
		 * @param raw 表示是否是“生操作”，“生操作”用于导入导出操作的rawAdd，在添入note时不进行相关关联操作（不重设时间戳、不计算workLoad、不查重复）
		 */@Override protected synchronized boolean mAdd(NoteStream toAdds,boolean raw) {
			if(super.mAdd(toAdds,raw)){
				if (!raw && getMemoryPlan() != null) {
					float workLoad=0;
					toAdds.begin();
					while (toAdds.goNext()) {
						LocalNote note = toAdds.get();
						workLoad += PlanProgress.computeNoteWorkLoad(
							getMemoryPlan().args,note.getNoteProgress(),note.getNoteBody().getWorkLoadRate());
					}
					modifyPlanProgressWorkLoadByOffset(workLoad);
				}
				return true;
			}
			return false;
		}
		/**
		 *  delete操作的中介方法，用于处理内部事务，考虑了workLoad变化
		 * @return 若删除成功则返回被删除的对象，若失败则返回null
		 */@Override protected synchronized LocalNote mDelete(LocalNote note) {
			LocalNote re=super.mDelete(note);
			if (re != null && getMemoryPlan() != null) {
				float workLoad = PlanProgress.computeNoteWorkLoad(
					getMemoryPlan().args, note.getNoteProgress(), note.getNoteBody().getWorkLoadRate());
				modifyPlanProgressWorkLoadByOffset(-workLoad);
			}
			return re;
		}
	}

	public static class MemoryPlan{
		@NonNull
		public final PlanArgs args;
		@NonNull
		public final PlanProgress progress;

		public MemoryPlan(@NonNull PlanArgs args, @NonNull PlanProgress progress) {
			this.args = args;
			this.progress = progress;
		}
		public MemoryPlan(PlanArgs planArgs,long nowTime) {
			this(planArgs, new PlanProgress(nowTime));
		}


		//getters
		public boolean isEnabled(){
			return progress.isEnabled();
		}
		public float getFreedWorkLoad(){
			return args.workLoadLimit-progress.workLoad;
		}
		/**
		 * @param nowTime 现在的UTC时间
		 * @return 下一次要重新填充计划的UTC时间，若为0则表示永远不填充（已经修正暂停偏移）
		 */public long getNextRefillTime(long nowTime) {
			if(args.refillInterval == refillInterval_never || !progress.isEnabled()) {
				return 0;
			} else {
				long dis = nowTime - progress.lastRefillTime;
				if (dis > 0 && dis < args.refillInterval) {
					return args.refillInterval + progress.lastRefillTime;
				}
				return nowTime;
			}
		}

		//compute
		public long getNeededMemoryTimeLine(long nowTime) {
			return progress.getNeededMemoryTimeLine(nowTime);
		}
		public int computeRefillCount(long nowTime) {
			return progress.checkRefillCount(args, nowTime);
		}

		//modify
		public MemoryPlan setArgs(@NonNull PlanArgs newArgs) {
			return new MemoryPlan(newArgs, progress);
		}
		public MemoryPlan setProgress(@NonNull PlanProgress newProgress) {
			return new MemoryPlan(args, newProgress);
		}

		public MemoryPlan setPaused(long nowTime) {
			return new MemoryPlan(args, progress.makePaused(nowTime));
		}
		public MemoryPlan setEnabled(long nowTime) {
			return new MemoryPlan(args, progress.makeEnabled(nowTime));
		}
	}



	public static final int progress_none=-1;
	public static final int progress_finish=-2;
	public static final int progress_begin=0;
	public static final int progress_unit=100;
	public static final int progress_decrease=progress_unit*3;
	public static final long nextTime_never=0;
	public static NoteProgress getNullProgress() { return new NoteProgress(progress_none, nextTime_never); }
	public static NoteProgress getBeginProgress(long nowTime) { return new NoteProgress(progress_begin, nowTime); }
	public static NoteProgress getFinishProgress() { return new NoteProgress(progress_finish, nextTime_never); }
	public static class NoteProgress implements Cloneable {
		public NoteProgress(int progress, long nextTime){
			this.progress = progress;
			this.nextTime = nextTime;
		}

		public static NoteProgress getMaximum(@Nullable NoteProgress progress1, @Nullable NoteProgress progress2) {
			if (progress1 == null) {
				return progress2;
			}else if (progress2 == null) {
				return progress1;
			}
			if (progress1.isFinished()) {
				return progress1;
			} else if (progress2.isFinished()) {
				return progress2;
			}
			if (!progress1.isPlanned()) {
				return progress2;
			}else if (!progress2.isPlanned()) {
				return progress1;
			}
			return progress1.progress > progress2.progress ? progress1 : progress2;
		}
		public static NoteProgress getMinimum(@Nullable NoteProgress progress1, @Nullable NoteProgress progress2) {
			if (progress1 == null || progress2 == null) {
				return null;
			}
			if (!progress1.isPlanned()) {
				return progress1;
			}else if(!progress2.isPlanned()) {
				return progress2;
			}
			if (progress1.isFinished()) {
				return progress2;
			} else if (progress2.isFinished()) {
				return progress1;
			}
			return progress1.progress < progress2.progress ? progress1 : progress2;
		}


		/**
		 * 表示复习进度，每背一次就加100，-1表示尚未加入计划，-2表示已完成学习
		 */public final int progress;
		/**
		 * 下一次要复习的时间，0表示不用复习（或还没加入计划）
		 */public final long nextTime;


		/**
		 * @return 是否是在正在学习的状态。若未加入过队列或已经学习完成则返回false
		 */public boolean isLearning(){
			return progress >=progress_begin;
		}
		/**
		 * @return 是否已被加入到memory队列，true表示在队列中（可能在学习，也可能已学完了）
		 */public boolean isPlanned(){
			return progress != progress_none;
		}
		/**
		 * @return 是否已学完了
		 */public boolean isFinished(){
			//todo 关于note的finish需要一些判断机制
			return progress == progress_finish;
		}


		/**
		 * 当背的时候记住了，可以用这个方法计算
		 */public NoteProgress getIncreased(PlanArgs args,long nowTime) {
			int newProgress=progress+ progress_unit;
			return new NoteProgress(newProgress,args.computeNextTime(nowTime, newProgress));
		}
		/**
		 * 当背的时候忘记了，可以用这个方法计算
		 */public NoteProgress getDecreased(PlanArgs args,long nowTime) {
			int newProgress=progress- progress_decrease;
			if(newProgress<0) newProgress=0;
			return new NoteProgress(newProgress,nowTime+1);
		}

		public NoteProgress getClone(){
			try {
				return (NoteProgress) this.clone();
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
			return null;
		}
	}

	public enum PlanState { enabled, disabled}
	public static class PlanProgress {
		public PlanProgress(PlanState state, float workLoad, long createTime, long lastRefillTime, long lastPausedTime, long pausedTimeOffset) {
			this.state = state;
			this.workLoad = workLoad;
			this.createTime = createTime;
			this.lastRefillTime = lastRefillTime;
			this.lastPausedTime = lastPausedTime;
			this.pausedTimeOffset = pausedTimeOffset;
		}
		private PlanProgress(long nowTime) {
			state = PlanState.enabled;
			this.workLoad=0;
			this.createTime = nowTime;
			this.lastRefillTime = nowTime;
			this.lastPausedTime=nowTime;
			this.pausedTimeOffset =0;
		}

		public final PlanState state;
		/**
		 * 一个用于衡量计划工作强度的值，用于防止过量积累。其值为平均下来的一天要复习的单词数（不包括未加的新单词）
		 */public final float workLoad;
		public final long createTime;
		/**
		 * 上次填充计划的时间，已修正暂停偏差
		 */public final long lastRefillTime;
		/**
		 * 等效的上一次添加单词的时候（在暂停时已经自动偏移，不用考虑暂停偏移）
		 */public final long lastPausedTime;
		/**
		 * 因为暂停计划而产生的时间偏差，在计算单词的复习时间时应考虑这个偏差
		 */public final long pausedTimeOffset;


		//getters
		public boolean isEnabled() {
			return state == PlanState.enabled;
		}


		//computing
		public int checkRefillCount(PlanArgs args,long nowTime) {
			long timeDis=nowTime-lastRefillTime;
			return (int) Math.floor(timeDis/args.refillInterval);
		}
		/**
		 * @return 用于根据noteProgress来衡量一个note是否需要复习了。
		 * 注意此处的返回值与nowTime可能不一致，因为当计划被暂停时会产生暂停偏差
		 */public long getNeededMemoryTimeLine(long nowTime) {
			return nowTime-pausedTimeOffset;
		}
		public static float computeNoteWorkLoad(long noteReviewInterval,float rate) {
			float aDay=24*3600*1000;
			float workLoad;
			if (noteReviewInterval == 0) {
				workLoad= singleNewWordWorkLoad;
			}else{
				workLoad=aDay/noteReviewInterval;
			}
			if (workLoad > singleNewWordWorkLoad) {
				workLoad= singleNewWordWorkLoad;
			}
			return workLoad*rate;
		}
		public static float computeNoteWorkLoad(PlanArgs args, @Nullable NoteProgress progress, float rate) {
			if (progress != null) {
				if (progress.progress == Memory.progress_finish || progress.progress == Memory.progress_none) {
					return 0;
				}else {
					return computeNoteWorkLoad(args.computeNextTime(0, progress.progress), rate);
				}
			}
			return 0;
		}


		//setters
		public PlanProgress makePaused(long nowTime) {
			return new PlanProgress(PlanState.disabled, workLoad, createTime, lastRefillTime, nowTime, pausedTimeOffset);
		}
		public PlanProgress makeEnabled(long nowTime) {
			long length=nowTime-lastPausedTime;
			return new PlanProgress(PlanState.enabled, workLoad, createTime,lastRefillTime+length,lastPausedTime, pausedTimeOffset + length);
		}

		/**
		 * 直接设置lastRefillTime，一般少用
		 */protected PlanProgress setLastRefillTime(long newLastRefillTime) {
			return new PlanProgress(state, workLoad, createTime, newLastRefillTime, lastPausedTime, pausedTimeOffset);
		}
		/**
		 * 按照时间来refill。会将lastRefilledTime设置到接近nowTime的地方（之间相差不大于一个refillInterval）
		 * 仅将时间调整，不考虑其他东西！
		 */protected PlanProgress getRefilled(PlanArgs args, long nowTime) {
			return new PlanProgress(state, workLoad, createTime,
				lastRefillTime+args.refillInterval*checkRefillCount(args, nowTime),
				lastPausedTime, pausedTimeOffset);
		}

		public PlanProgress setWorkLoad(float newWorkLoad) {
			if (newWorkLoad < 0) newWorkLoad = 0;
			return new PlanProgress(state, newWorkLoad, createTime, lastRefillTime, lastPausedTime, pausedTimeOffset);
		}
		public PlanProgress increaseWorkLoad(float offset) {
			return setWorkLoad(workLoad + offset);
		}
	}



	//参数标准
	public static final float workLoadLimit_default = 60;
	/**
	 * 不公开的参数，表示单一的一个单词的workLoad最大值（例如马上就得复习的单词们就是这个值），对workLoad统计有一定影响
	 */public static final float singleNewWordWorkLoad =3;
	public static final int refillInterval_default =2*3600*1000;
	public static final int refillInterval_never=-1;
	public static final float arg_times_default = 20;
	public static final float arg_k_default = 1;
	public static final float arg_a_default =2f;
	public static PlanArgs getDefaultPlanArgs() {
		return new PlanArgs(workLoadLimit_default, refillInterval_default, arg_times_default, arg_k_default, arg_a_default);
	}
	public static class PlanArgs{
		/**
		 * 用于防止过量积累的工作量限制值，其值表示平均下来一天需要复习的单词数的最高值
		 */final public float workLoadLimit;
		/**
		 * 每次将新词添加进计划的时间间隔（以毫秒为单位），-1表示不再添加
		 */final public long refillInterval;
		final public float arg_times;
		final public float arg_k;
		final public float arg_a;

		public PlanArgs(float workLoadLimit, long refillInterval, float arg_times, float arg_k, float arg_a) {
			this.workLoadLimit = workLoadLimit;
			this.refillInterval = refillInterval;
			this.arg_times = arg_times;
			this.arg_k = arg_k;
			this.arg_a = arg_a;
		}
		public PlanArgs setRefillInterval(long newRefillInterval) {
			return new PlanArgs(workLoadLimit, newRefillInterval, arg_times, arg_k, arg_a);
		}
		public PlanArgs setWorkLoadLimit(float newWorkLoadLimit) {
			return new PlanArgs(newWorkLoadLimit, refillInterval, arg_times, arg_k, arg_a);
		}
		public long computeNextTime(long nowTime, int progress) {
			if (progress == -1 || progress == -2) {
				return 0;
			}
			return nowTime + computeReviewTimeInterval(progress, progress_unit, arg_k, arg_a, default_random);
		}
	}


	//算法
	/**
	 * 不公开的算法参数，用于使单词变得随机乱序
	 */public static final float default_random=0.07f;
	/**
	 * 这是一个基于记忆曲线的算法，用于计算一个这次成功记住了的单词到下次需要复习的时间间隔
	 * @param progress 复习的进度信息
	 * @param progressUnit 复习进度的单位制，即为一次学习所增加的进度量（为了精细化，progress可以不是unit的整数倍）
	 * @param k 一个参数，用于调整输出length的倍数，默认为1（即不调整）
	 * @param a 一个参数，是length随progress递增而递增的指数关系，默认为2
	 * @param ran 一个参数，用于使每次计算的length在一定范围内浮动，
	 *               其数值为前后浮动的比例（例如0.3表示在0.7~1.3内浮动）。
	 *               用于实现单词的乱序排列。默认为0.05
	 * @return 距离下一次需要复习的时间
	 */static private long computeReviewTimeInterval(int progress, int progressUnit, float k, float a, float ran) {
		double length=k * Math.pow((float)progress/progressUnit , a)*1000 * 3600;
		length=((Math.random()*ran*2-ran)+1)*length;
		return (long) length;
	}

}
